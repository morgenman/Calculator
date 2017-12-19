
// Dylan Morgen
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;

public class Calculator {
	static ArrayList<Element> variables = new ArrayList<Element>();// holds all user variables

	public static void main(String[] args) {
		String input;
		Scanner scan = new Scanner(System.in); //get user input
		do {
			input = scan.nextLine(); //get line
			if (!input.isEmpty()) Line(input); //process line
		}
		while (!input.contains("exit")); //if user types exit 
		scan.close();
	}

	private static void Line(String in) {
		boolean out = true;
		if (in.contains("clear")) { //clear var
			if (in.contains("all")) {
				variables = new ArrayList<Element>(); //clear all variables
			}
			int size = variables.size();
			boolean didsomething = false; // if it didn't delete a variable, tell the user
			for (int i = 0; i < size; i++) {
				if ((Arrays.asList(in.split("\\s+|,\\s+|,")).contains(variables.get(i).value()))) { //matches variable names reguardless of spaces or ','. regex
					didsomething = true; // do something
					System.out.println(variables.get(i).value() + " has been cleared"); //variable name
					variables.remove(i); // remove the variable
					size = variables.size(); // prevents 
					i--; // out of bounds fix, because removing slides it over to left 
				}
			}
			if (!didsomething) System.out.println("No matching variables found");
		}
		else {
			LinkedList<Element> line = convert(in); // separate string into list of elements, adding variables to variables
			if (!Valid(parse(line))) { // if invalid
				out = false; //don't output a value
				int size = variables.size(); //allows for modification of range inside iterative loop
				for (int i = 0; i < size; i++) {
					if (!variables.get(i).isAssigned()) { //if invalid, don't keep variables in variable array
						variables.remove(i);
						size--;
						i--;
					}
				}
			}
			else {
				float value = evaluate(parse(line)); //parse converts to proper math notation, evaluate converts to postscript and solves 
				for (Element i : variables) {
					if (!i.isAssigned()) {
						if (i.getVariable().contains("all") || i.getVariable().contains("All") || i.getVariable()
						        .contains("ALL")) { // don't allow certain variable names
							System.out.println("Cannot use variable name with all in it");
						}
						else {
							i.varval(value); //assign value
							i.setAssigned(true);
							out = false; //don't output if assignment statement
							System.out.print(i.getVariable() + "=" + i.varval() + "\n");// output variable assignment
						}
					}
				}
				if (out) System.out.println(value);
			}
		}
	}

	private static void display(LinkedList<Element> in) {
		//unused, but outputs the list for debugging purposing
		for (int i = 0; i < in.size(); i++) {
			System.out.print(in.get(i).value() + ", ");
		}
	}

	private static LinkedList<Element> parse(LinkedList<Element> in) {
		// filter out variable assignment elements
		LinkedList<Element> val = new LinkedList<Element>();
		if (in.size() == 1) return in;
		for (int i = 0; i < in.size(); i++) {
			if (in.get(i).getType() == 3) {
				if (i != in.size() - 1 && in.get(i + 1).value().contains("=")) {
					addVar(variables, in.get(i)).setAssigned(false); //add variables
					i++;//if it has something like a=5, remove a=
				}
				else if (i == in.size() - 1 && in.get(i - 1).value().contains("=")) {
					addVar(variables, in.get(i)).setAssigned(false); //add variables
				}
				else {
					val.add(in.get(i));//keep all other elements 
				}
			}
			else if (!in.get(i).value().contains("=")) {
				val.add(in.get(i)); // add anything else that's not '='
			}
		}
		return val;
	}

	private static float evaluate(LinkedList<Element> in) {
		//convert to RPL and solve
		float out = 0;
		Stack<Element> stack = new Stack<Element>(); //stack
		LinkedList<Element> queue = new LinkedList<Element>(); //queue
		//convert to rpl
		for (Element i : in) {
			//basic infix to RPL conversion rules, based on precidence
			switch (i.getType()) {
			case 1:
			case 3:
				queue.add(i);
				break;
			case 2:
				while (!stack.isEmpty() && stack.peek().getPrec() >= i.getPrec()) {
					queue.add(stack.pop());
				}
				stack.push(i);
				break;
			case 5:
				stack.push(i);
				break;
			case 6:
				while (!stack.isEmpty() && identify(stack.peek().value().charAt(0)) != 5) {
					queue.add(stack.pop());
				}
				stack.pop();
			}
		}
		while (!stack.isEmpty()) { //add all remaining elements in stack to queue
			queue.add(stack.pop());
		}
		//process queue, solving from left to right, starting at the third element
		while (queue.size() != 1) {
			for (int i = 0; i < queue.size(); i++) {
				if (i < 2 && queue.get(i).getType() == 2 && (queue.get(i).value().contains("+") || queue.get(i).value()
				        .contains("-"))) {
					queue.add(0, new Element("0")); //if the original expression starts with a negative number, add a 0
				}
				else if (!(queue.get(i).getType() == 1 || queue.get(i).getType() == 3)) {
					Element val1 = queue.remove(i - 2);
					Element val2 = queue.remove(i - 2);
					Element oper = queue.remove(i - 2); // for every operator, remove last two values
					if (val1.getType() == 3) val1 = addVar(variables, val1); //if either are a variable, get the value
					if (val2.getType() == 3) val2 = addVar(variables, val2);
					queue.add(i - 2, oper.use(val1, val2));
					i -= 2;
				}
			}
		}
		if (queue.peek().getType() == 3) out = addVar(variables, queue.pop()).varval();//if it's a variable, get the value of it(useful if the input is only one variable)
		else out = queue.pop().varval();
		return out;
	}

	private static Element addVar(ArrayList<Element> in, Element in1) {
		// adds a variable to variables if it isn't already, and returns it from variables
		for (Element i : in) {
			if (i.getVariable().equals(in1.getVariable())) return i;
		}
		in.add(in1);
		for (Element i : in) {
			if (i.getVariable().equals(in1.getVariable())) return i;
		}
		return null;
	}

	private static boolean contains(ArrayList<Element> in, Element in1) {
		//simle check to see if the database already has the variable
		for (Element i : in) {
			if (i.getVariable().equals(in1.getVariable())) return true;
		}
		return false;
	}

	private static boolean Valid(LinkedList<Element> in) {
		//checks for certain errors like bracket mix match and the like. Each chekc should be relatively self explanitory
		Element curr;
		Element next;
		Element prev = new Element("NULL");
		boolean valid = true;
		int bracket = 0;
		for (int i = 0; i < in.size(); i++) {
			curr = in.get(i);
			if (curr.getType() == 5) bracket++;
			if (curr.getType() == 6) bracket--;
			if (bracket < 0) {
				System.out.println("Error, bracket mismatch");
				return false;
			}
			if (i < in.size() - 1) {
				next = in.get(i + 1);
				if (curr.getType() == next.getType()) {
					switch (curr.getType()) {
					case 1:
						System.out.println("Error, No operator between " + curr.value() + " and " + next.value());
						return false;
					case 2:
						if (!((curr.value().equals("+") || curr.value().equals("-")) && (next.value().equals("+")
						        || next.value().equals("-")))) {
							System.out.println("Error, No operator between " + curr.value() + " and " + next.value());
							return false;
						}
						break;
					case 3:
						System.out.println("Error, No operator between " + curr.value() + " and " + next.value());
						return false;
					}

				}
				else if (curr.getType() == 3) {
					if (addVar(variables, curr).isAssigned() == false) {
						System.out.println("Error, variable not defined");
						return false;
					}
					else if (curr.value().contains("all")) {
						System.out.println("Variable name cannot contain 'all'");
						return false;
					}

				}
			}
			else if (curr.getType() == 3) {
				if (addVar(variables, curr).isAssigned() == false) {
					System.out.println("Error, variable not defined");
					return false;
				}
			}
			else if (curr.getType() == 2) {
				System.out.println("Error, cannot end with operator");
				return false;
			}
		}
		if (bracket == 0) return true;
		else {
			System.out.println("Error, bracket mismatch");
			return false;
		}
	}

	private static LinkedList<Element> convert(String in) {
		// convert to math notation, compatible with infix converter. eg
		// 5/-3 --> 5/(0-3) 
		// 5+++- +- -3 --> 5-3
		LinkedList<Element> LinkedList = new LinkedList<Element>();
		int endlist = in.length();
		int start = 0;
		int end = 0;
		int type = 0;// This refers to the type of the last element
		int sign = 0;
		//analyses the previous element and current for comparison
		for (int i = 0; i < endlist; i++) {
			if (type == 0) {
				type = identify(in.toCharArray()[i]);
				start = i;
				end = i + 1;
				if (endlist == 1) {
					LinkedList.add(new Element(in.substring(start, end))); //adds the number to the output
				}
			}
			else if (type == identify(in.toCharArray()[i]) && (type == 1 || type == 3)) {
				// if they are the same type and a character type/number then string them together (don't add yet)
				end = i + 1;
				if (i == endlist - 1) {
					LinkedList.add(new Element(in.substring(start, end)));
				}
			}
			else if (type != 4) {

				if (i == endlist - 1) {
					//if its the last element, submit it
					LinkedList.add(new Element(in.substring(start, end)));
					start = i;
					end = i + 1;
				}
				// (a)2--> (a)*2 and (a)b--> (a)*b
				if ((identify(in.toCharArray()[i]) == 3 || identify(in.toCharArray()[i]) == 1) && (type == 6))
				    LinkedList.add(new Element("*"));
				// 3a --> 3*a and a3 --> a*3
				if (identify(in.toCharArray()[i]) == 3 && type == 1 || (identify(in.toCharArray()[i]) == 1
				        && type == 3)) LinkedList.add(new Element("*"));
				LinkedList.add(new Element(in.substring(start, end)));
				// (a)(b)--> (a)*(b) and 2(a)--> 2*(a)
				if (identify(in.toCharArray()[i]) == 5 && (type == 1 || type == 3 || type == 6)) LinkedList.add(
				        new Element("*"));

				type = identify(in.toCharArray()[i]);
				start = i;
				end = i + 1;
			}
			else if (type == 4) {
				//if its a space don't do anything
				type = identify(in.toCharArray()[i]);
				start = i;
				end = i + 1;
				if (i == endlist - 1 && (identify(in.toCharArray()[i]) != 4)) LinkedList.add(new Element(in.substring(
				        start, end)));
			}
		}
		//removes extra + and - eg:  ++ - +-+-5 --> 0-5
		LinkedList<Element> removedupes = new LinkedList<Element>();
		for (int i = 0; i < LinkedList.size(); i++) {

			switch (LinkedList.get(i).getType()) {
			case 1:
				removedupes.add(LinkedList.get(i));
				break;
			case 2:
				if (LinkedList.get(i).value().contains("+")) {
					if (removedupes.isEmpty() || i == 0 || removedupes.peekLast().getType() == 5 || removedupes
					        .peekLast().value().contains("=") || removedupes.peekLast().value().contains("*")
					        || removedupes.peekLast().value().contains("/") || removedupes.peekLast().value().contains(
					                "+") || removedupes.peekLast().value().contains("-")) {
					}
					else removedupes.add(LinkedList.get(i));
				}
				else if (LinkedList.get(i).value().contains("-")) {
					if (i == 0 || removedupes.isEmpty() || removedupes.peekLast().getType() == 5 || removedupes
					        .peekLast().value().contains("=") || removedupes.peekLast().value().contains("*")) {
						removedupes.add(new Element("0"));
						removedupes.add(new Element("-"));
					}
					else if (removedupes.peekLast().value().contains("/")) {
						removedupes.add(new Element("("));
						removedupes.add(new Element("0"));
						removedupes.add(LinkedList.get(i));
						i++;
						removedupes.add(LinkedList.get(i));
						removedupes.add(new Element(")"));
					}
					else if (removedupes.peekLast().getType() == 1 || removedupes.peekLast().getType() == 3) removedupes
					        .add(new Element("-"));
					else if (removedupes.peekLast().value().contains("-")) {
						removedupes.removeLast();
						removedupes.add(new Element("+"));
					}
					else if (removedupes.peekLast().value().contains("+")) {
						removedupes.removeLast();
						removedupes.add(new Element("-"));
					}
				}
				else removedupes.add(LinkedList.get(i));
				break;
			case 3:
				removedupes.add(LinkedList.get(i));
				break;
			case 4:
				removedupes.add(LinkedList.get(i));
				break;
			case 5:
				removedupes.add(LinkedList.get(i));
				break;
			case 6:
				removedupes.add(LinkedList.get(i));
				break;
			default:
				removedupes.add(LinkedList.get(i));
				break;
			}
		}
		return removedupes;
	}

	private static int identify(char in) {
		//used to identify types of chars
		if ((in >= '0' && in <= '9') || in == '.') return 1;
		else {
			switch (in) {
			case '*':
			case '/':
			case '+':
			case '-':
			case '=':
				return 2;
			case ' ':
				return 4;
			case '[':
			case '{':
			case '(':
				return 5;
			case '}':
			case ']':
			case ')':
				return 6;
			default:
				return 3;
			}
		}
	}
}
