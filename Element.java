// Element class, holds variables, operators, and numbers

public class Element {
	private float	floatval;					//actual numerical value
	private char	symbol;						//if its an operator
	private String	variable	= new String();	//variable name
	private int		type;						// 1 = float, 2 = operator, 3 = variablename etc;
	private boolean	assigned	= false;		//if variable has had a value put to it yet

	public boolean isAssigned() {
		return assigned;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public Element(String in) {
		// parse input 
		try {
			floatval = Float.valueOf(in); //if its a number
			type = 1;
		}
		catch (java.lang.NumberFormatException ex) {
			switch (in.toCharArray()[0]) {
			case '*':
			case '/':
			case '+':
			case '-':
			case '=':
				symbol = in.toCharArray()[0];
				type = 2;
				break;
			case '(':
			case '{':
			case '[':
				symbol = in.toCharArray()[0];
				type = 5;
				break;
			case ')':
			case '}':
			case ']':
				symbol = in.toCharArray()[0];
				type = 6;
				break;
			default:
				variable = in;
				type = 3;
			}
		}
	}

	public int getPrec() {
		//precidence for RPN
		switch (symbol) {
		case '*':
			return 3;
		case '/':
			return 3;
		case '+':
			return 2;
		case '-':
			return 2;
		case '(':
		case '{':
		case '[':
			return 0;
		default:
			System.out.println(symbol);
			System.out.println("erororororor");
			return 0;
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String value() {
		// return the value in string form
		switch (type) {
		case 1:
			return String.valueOf(floatval);
		case 2:
			return String.valueOf(symbol);
		case 3:
			return String.valueOf(variable);
		case 5:
		case 6:
			return String.valueOf(symbol);
		default:
			return "error";

		}
	}

	public float varval() {
		//value in number form
		return floatval;
	}

	public void varval(float val) {
		floatval = val;
	}

	public void addval(float val) {
		floatval += val;
	}

	public Element use(Element val1, Element val2) {
		//do the math
		switch (symbol) {
		case '*':
			floatval = val1.varval() * val2.varval();
			break;
		case '/':
			if (val2.varval() == 0) {
				System.out.println("Error, cannot divide by 0");
				throw new IllegalArgumentException();
			}
			floatval = val1.varval() / val2.varval();
			break;
		case '+':
			floatval = val1.varval() + val2.varval();
			break;
		case '-':
			floatval = val1.varval() - val2.varval();
			break;
		default:
			System.out.println(symbol);
			System.out.println("erororororor1");

		}
		this.type = 1;
		return this;
	}
}
