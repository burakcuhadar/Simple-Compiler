import java.util.*;
import java.io.*;

public class Main{
	//post-fix state of the line being processed
	static ArrayList<String> postfix = new ArrayList<String>(); 
	//defined variables
	static HashMap<String, String> variables = new HashMap<String, String>();	
	//stores index of line being processed
	static int lineNum = 0;
	//append this to a variable's name to differentiate it from a variable with the same name ignoring the case
	static int endNum = 0; 
	//put global variables, labels and function names in output file into this array to prevent collisions with variable names in the input file
	static String[] reserved = {"mulparam1","mulparam2","mulparam3","mulparam4","powparam1","powparam2","powparam3",
								"powparam4","powparam5","powparam6","pointer1","pointer2","pointer3","printparam1",
								"printparam2","output","start","addition","mult","power","forloop","squarep2",
								"nocarry","print","chooseparam","seperate","convert","digit","next","exit"};

	public static void main(String [] args)
			throws FileNotFoundException
	{
		if(args.length != 1)
		{
			System.out.println("Wrong number of arguments");
			return;
		}
		//stores name of the input file
		String input = args[0];
		//scanner for input file
		Scanner in = new Scanner(new File(input));
		//generate output file name for the given input file
		String output = "";
		int index = input.lastIndexOf('.');
		//if there is not "." in input file name
		if(index == -1)
		{
			output += input + ".asm";
		}
		//else name of the output file is input file name with ".asm" at the end
		else
		{
			output += input.substring(0, input.lastIndexOf('.')) + ".asm";
		}
		//printstream to write to the output file
		PrintStream out = new PrintStream(new File(output));
		//initializing global variables 
		out.println("jmp start\n"
				//These mulparams are created to store 16-bit parts of 32-bit multiplicands
				+"\tmulparam1 dw 0h\n"		    
				+"\tmulparam2 dw 0h\n"          
				+"\tmulparam3 dw 0h\n"
				+"\tmulparam4 dw 0h\n"
				//These powparams are used to store 16-bit parts of 32-bit base and exponent
				+"\tpowparam1 dw 0h\n"
				+"\tpowparam2 dw 0h\n"
				+"\tpowparam3 dw 0h\n"
				+"\tpowparam4 dw 0h\n"
				+"\tpowparam5 dw 0h\n"
				+"\tpowparam6 dw 0h\n"
				//These pointers stores location of the code after function calls
				+"\tpointer1  dw 0h\n"		    
				+"\tpointer2  dw 0h\n"
				+"\tpointer3  dw 0h\n"
				//These printparams stores 2 16-bit parts of 32-bit input that will be printed
				+"\tprintparam1  dw 0h\n"		    
				+"\tprintparam2  dw 0h\n"
				//output variable stores string format of hex numbers
				+"\toutput    db 8 dup 0h, 13, 10, \"$\"\n" );
		//defining functions 
		//addition function, pops four 16-bit numbers(two 32-bit numbers) from stack
		//after adding them, pushes the result in two 16-bit parts into stack
		out.println("addition proc\n"
				//pop and save the last element into variable pointer1
				//it is the address of the line after the function call
				+"\tpop pointer1\n"
				//save four 16-bit parts into registers
				+"\tpop ax\n"
				+"\tpop bx\n"
				+"\tpop cx\n"
				+"\tpop dx\n"
				//add least significant parts first
				+"\tadd ax, cx\n"
				//if there is carry add it to the result of most significant parts
				+"\tadc bx, dx\n"
				//push the result in two 16-bit parts back into stack
				+"\tpush bx\n"
				+"\tpush ax\n"
				//push pointer1 variable back into the stack so we can continue
				//when the function ends
				+"\tpush pointer1\n"
				+"\tret\n"
				+"addition endp\n");
		//multiplication function, pops four 16-bit numbers(two 32-bit numbers) from stack
		//after multiplying them, pushes the result in two 16-bit parts into stack
		//each number is divided into two parts:(a*2^16+b)*(c*2^16+d)
		//since the result will not exceed 32 bits we do not multiply a with c
		out.println("mult proc\n"
				//pop and save the last element into variable pointer2
				//it is the address of the line after the function call
				+"\tpop pointer2\n"
				//save four 16-bit parts into variables mulparam1-4
				+"\tpop mulparam1\n"
				+"\tpop mulparam2\n"
				+"\tpop mulparam3\n"
				+"\tpop mulparam4\n"
				//to multiply, move mulparam1 to ax register
				+"\tmov ax, mulparam1\n"
				//multiply mulparam3 (b*d)
				+"\tmul mulparam3\n"
				//push overflow and result to stack
				+"\tpush dx\n"
				+"\tpush ax\n"
				//multiply mulparam2 with mulparam3 (a*d)
				+"\tmov ax, mulparam2\n"
				+"\tmul mulparam3\n"
				//to multiply the result with 2^16 push 0000h after the result
				+"\tpush ax\n"
				+"\tpush 0000h\n"
				//right now b*d and a*d*2^16 are in stack, add them
				+"\tcall addition\n"
				//multiply mulparam1 with mulparam4 (c*b)
				+"\tmov ax, mulparam1\n"
				+"\tmul mulparam4\n"
				//to multiply the result with 2^16 push 0000h after the result
				+"\tpush ax\n"
				+"\tpush 0000h\n"
				//right now (b*d + a*d*2^16) and c*b*2^16 are in stack, add them
				//Note: the result will be pushed in addition so we do not have push it
				+"\tcall addition\n"
				//push pointer2 variable back into the stack so we can continue
				//when the function ends
				+"\tpush pointer2\n"
				+"\tret\n"
				+"mult endp\n");
		//power function, pops four 16-bit numbers(two 32-bit numbers) from stack
		//after taking power, pushes the result in two 16-bit parts into stack
		out.println("power proc\n"
				//pop and save the last element into variable pointer2
				//it is the address of the line after the function call
				+"\tpop pointer3\n"
				//save four 16-bit parts into variables powparam1-4
				+"\tpop powparam3\n"
				+"\tpop powparam4\n"
				+"\tpop powparam1\n"
				+"\tpop powparam2\n"
				//the result(p) will be saved into powparam5-6
				//at the beginning it is 1
				+"\tmov powparam5, 01h\n"
				+"\tmov powparam6, 0h\n"
				//in this for loop powparam1-2(n) is divided by 2 at every iteration
				//until it is 0. powparam3-4(p2) is squared and if n is odd p is updated
				//by multiplying it with p2 at each step.
				+"forloop:\n"
				//check whether n is odd
				+"\tmov ax, powparam1\n"
				+"\tand ax, 01h\n"
				//if it is not take square of p2 ( powparam3-4)
				+"\tjz squarep2\n"
				//if it is odd p = p * p2
				+"\tpush powparam6\n"
				+"\tpush powparam5\n"
				+"\tpush powparam4\n"
				+"\tpush powparam3\n"
				+"\tcall mult\n"
				+"\tpop powparam5\n"
				+"\tpop powparam6\n"
				//take square of p2
				+"squarep2:\n"
				+"\tpush powparam4\n"
				+"\tpush powparam3\n"
				+"\tpush powparam4\n"
				+"\tpush powparam3\n"
				+"\tcall mult\n"
				+"\tpop powparam3\n"
				+"\tpop powparam4\n"
				//divide n(powparam1-2) by two:
				//shift each part of n to the right bitwise
				+"\tshr powparam1, 1\n"
				+"\tshr powparam2, 1\n"
				//check whether there is a carry when we shift most significant part
				+"\tjnc nocarry\n"
				//if there is carry add 1000 to least significant part
				+"\tadd powparam1, 1000h\n"
				//if there is no carry, n is already divided by 2, compare it with 0
				+"nocarry:\n"
				+"\tcmp powparam2, 00h\n"
				//if least significant part of n is not 0, continue to for loop
				+"\tjnz forloop\n"
				//if most significant part of n is not 0, continue to for loop
				+"\tcmp powparam1, 00h\n"
				+"\tjnz forloop\n"
				//if we can reach here, it means both parts of the number n is 0
				//which means n is itself 0, so push the result in two parts
				+"\tpush powparam6\n"
				+"\tpush powparam5\n"
				//push pointer3 variable back into the stack so we can continue
				//when the function ends
				+"\tpush pointer3\n"
				+"\tret\n"
				+"power endp\n");
		//print function, pops two 16-bit numbers(one 32-bit numbers) from stack
		//after separating each 4-bit(one digit in hexadecimal) into bytes, converts
		//each byte into ascii code, then it prints the number
		out.println("print proc\n"
				//pop and save the last element into variable pointer1
				//it is the address of the line after the function call
				+"\tpop pointer1\n"
				//counter to loop two times to print two parts
				+"\tmov dl, 2d\n"
				//address of the output string is stored in bx
				+"\tmov bx, offset output\n"
				//two parts of the number to be printed is saved into variables
				+"\tpop printparam1\n"
				+"\tpop printparam2\n"
				//we decide which part of the number to print in this block
				+"chooseparam:\n"
				//save to the ax, to separate it later
				+"\tmov ax, printparam2\n"
				+"\tcmp dl,1\n"
				//if counter is 2 zero flag is not set so we jump to separate
				//meaning we write first the most significant part
				+"\tjnz seperate\n"
				//if counter is 1 zero flag is set so we change the variable
				//to be printed to least significant part, by that way we 
				//print it after most significant part
				+"\tmov ax, printparam1\n"
				+"seperate:\n"
				//take the first digit of the number in ax register
				+"\tmov cl, ah\n"
				+"\tshr cl, 4\n"
				//save it in output string
				+"\tmov b[bx], cl\n"
				+"\tinc bx\n"
				//take the second digit of the number in ax register
				+"\tmov cl, ah\n"
				+"\tshl cl, 4\n"
				+"\tshr cl, 4\n"
				//save it in output string
				+"\tmov b[bx], cl\n"
				+"\tinc bx\n"
				//take the third digit of the number in ax register
				+"\tmov cl, al\n"
				+"\tshr cl, 4\n"
				//save it in output string
				+"\tmov b[bx], cl\n"
				+"\tinc bx\n"
				//take the fourth digit of the number in ax register
				+"\tmov cl, al\n"
				+"\tshl cl, 4\n"
				+"\tshr cl, 4\n"
				//save it in output string
				+"\tmov b[bx], cl\n"
				+"\tinc bx\n"
				//decrement counter since we processed the number
				+"\tdec dl\n"
				//if counter is not zero jump at the start of the loop
				//to separate second part
				+"\tjnz chooseparam\n"
				//now set the counter 8 to process 8 bytes representing each digit
				+"\tmov dl, 8d\n"
				+"\tmov bx, offset output\n"
				//convert each digit to its ascii value
				+"convert:\n"
				//compare digit with Ah to determine if it is between 0-9 or A-F
				+"\tcmp b[bx], 0Ah\n"
				+"\tjnae digit\n"
				//if it is hex digit(A-F) add 37h( Ah + 37h = 41h(ascii value of 'A'))
				+"\tadd b[bx], 037h\n"
				+"\tjmp next\n"
				// if it is decimal digit add 30h(ascii value of '0')
				+"digit:\n"
				+"\tadd b[bx], 030h\n"
				//increment address to store next digit, decrement counter
				+"next:\n"
				+"\tinc bx\n"
				+"\tdec dl\n"
				//if count is zero end the loop, if not jump back
				+"\tjnz convert\n"
				//move the address of string to dx register to print it
				+"\tmov dx, offset output\n"
				//move 09 to ah register to print string
				+"\tmov ah, 09\n"
				//call interrupt
				+"\tint 21h\n"
				//push pointer3 variable back into the stack so we can continue
				//when the function ends
				+"\tpush pointer1\n"
				+"\tret\n"
				+"print endp\n");
		//start block
		out.println("start:");
		String line;
		//parsing and writing appropriate a86 code for each line
		while(in.hasNextLine())
		{
			lineNum++;
			line = in.nextLine();
			//first we parse the line so that we get the post-fix form of the line, if 
			//there is a syntax error it is also detected in parse function
			parse(line, "line");
			//if there is only one token it means it is a variable or a number
			if( postfix.size() == 1 )
			{
				//if it is variable
				if( isId(postfix.get(0)) )
				{
					//if variable is undefined
					if( !variables.containsKey(postfix.get(0)) )
					{
						out.println("\tpush 0000h");
						out.println("\tpush 0000h");
					}
					//if it is defined find the variable name from hashmap and push its two parts into stack
					else
					{
						out.println("\tpush w[offset " + variables.get(postfix.get(0)) + "]" );
						out.println("\tpush w[offset " + variables.get(postfix.get(0)) + " +2]" );
					}
				}
				//else it is a hex number
				else
				{
					String hexNum = postfix.get(0);
					//insert 0s at the start of the number to make it 32-bit long
					for(int j=0, l=hexNum.length(); j< 8-l; j++)
					{
						hexNum = "0" + hexNum;
					}
					//push the value in two 16-bit parts
					out.println("\tpush 0" + hexNum.substring(0,4) + "h");
					out.println("\tpush 0" + hexNum.substring(4,hexNum.length()) + "h");
				}
				//in each case we should print the values
				//call print to print two parts of the value of the variable
				out.println("\tcall print");
			}
			//else it can mean assignment or expression
			else if( postfix.size() > 1)
			{
				//setup the index for the following for loop which will traverse tokens in postfix form
				int i = 0;
				//where the for loop will end
				int n = postfix.size();
				//if this is an assignment(first token is variable to be assigned, last token is = sign),
				//ignore the first and last tokens. We will assign to the first token after the loop.
				if(postfix.get(postfix.size()-1).equals("="))
				{
					i = 1;
					n = postfix.size()-1;
				}
				//traverse the post-fix arraylist to push appropriate variables and call functions
				for(; i<n; i++)
				{
					String currElem = postfix.get(i);
					//if the current token in post-fix is an id and not the pow function
					if( isId( currElem ) && !currElem.equals("pow") )
					{
						//if variable is undefined
						if( !variables.containsKey( currElem ) )
						{
							//since it is undefined its value is 0 by default
							out.println("\tpush 0000h");
							out.println("\tpush 0000h");
						}
						//else variable is defined
						else
						{
							//we push the value of variable, after finding its name in hashmap
							out.println("\tpush w[offset " + variables.get(currElem) + "]" );
							out.println("\tpush w[offset " + variables.get(currElem) + " +2]" );
						}
					}
					//if it is num we will make it first 32-bit long and push it to the stack
					else if( isNum( currElem ))
					{
						//insert 0s at the start of the number to make it 32-bit long
						for(int j=0, l=currElem.length(); j< 8-l; j++)
						{
							currElem = "0".concat(currElem);
						}
						//push the value in two 16-bit parts
						out.println("\tpush 0" + currElem.substring(0,4) + "h");
						out.println("\tpush 0" + currElem.substring(4,currElem.length()) + "h");
					}
					//else it has to be "+", "*", or "pow". we call functions in these cases
					else if( currElem.equals("+"))
					{
						out.println("\tcall addition");
					}
					else if( currElem.equals("*"))
					{
						out.println("\tcall mult");
					}
					else if( currElem.equals("pow"))
					{
						out.println("\tcall power");
					}

				}
				//if this line is an assignment
				if(postfix.get(postfix.size()-1).equals("="))
				{
					//if variable is already defined update its value
					if( variables.containsKey( postfix.get(0)))
					{
						out.println("\tpop w[offset " + variables.get(postfix.get(0)) + " +2]" );
						out.println("\tpop w[offset " + variables.get(postfix.get(0)) + "]");
					}
					//else define the variable
					else
					{
						//lower case the variable's name as a86 is case insensitive
						String variable = postfix.get(0).toLowerCase();
						//check whether this variable has the same name with one of the reserved names in reserved array
						for(String reservedName : reserved)
						{
							if(variable.equalsIgnoreCase(reservedName))
							{
								//increment endNum until variable+endNum is different from other variables
								while( variables.containsValue(variable+endNum) )
								{
									endNum++;
								}
								//we append a number to variable's name to make it different from reserved names
								variable += endNum;
								//increment endNum to change it so that we can use it with another variable
								endNum++;
								//since we have already differentiated variable's name from a reserved name
								//we can break and continue to the definition of the variable
								break;
							}
						}
						//check whether this variable is same with another variable when we ignore case 	
						for(String key : variables.keySet() )
						{
							if( variable.equalsIgnoreCase(key))
							{
								//increment endNum until variable+endNum is different from other variables
								while( variables.containsValue(variable+endNum) )
								{
									endNum++;
								}
								//we append a number to make it distinct
								variable += endNum;
								//increment endNum to change it so that we can use it with another variable
								endNum++;
								//since we have already distinguished variable's name by appending number
								//we can break and continue to the definition of the variable
								break;
							}
						}
						
						//variable definition in a86
						out.println("\t" + variable +" dd 0h");
						//pop two elements from stack and save them into our newly defined variables
						out.println("\tpop w[offset " + variable +  " +2]");
						out.println("\tpop w[offset " + variable +  "]");
						//add the defined variable into variables map
						variables.put( postfix.get(0), variable);
					}
				}
				//else it means that there is only expression, print it
				else
				{
					out.println("\tcall print");
				}
			}

			postfix.clear();
		}
		//return to os
		out.println("exit:\n"
				+"\tmov ah, 4ch\n"
				+"\tmov al, 00\n"
				+"\tint 21h\n");
		in.close();
		out.close();
	}

	//converts line into post-fix form
	static void parse(String nonTerm, String type)
	{
		Scanner read;
		//if non-term is a line
		if(type.equals("line"))
		{
			int eqIndex = nonTerm.indexOf('=');
			//if assignment operator exists in line
			if(eqIndex != -1)
			{
				//read left-hand side
				read = new Scanner(nonTerm.substring(0, eqIndex));
				//number of tokens in left-hand side
				int numTokens = 0;
				//last token of left-hand side
				String last = "";
				while(read.hasNext())
				{
					numTokens++;
					last = read.next();
				}
				//if number of tokens in left-hand side is not one
				if(numTokens != 1)
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
				//if the token is not a legal variable name
				if(!isId(last))
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}

				//right-hand side is expression
				String expr = nonTerm.substring(eqIndex+1);
				//add variable to post-fix, parse expression, and add "=" to post-fix
				postfix.add(last);
				parse(expr, "expression");
				postfix.add("=");
				read.close();
			}
			//if assignment operator does not exist
			else
			{
				read = new Scanner(nonTerm);
				//if line is empty
				if(!read.hasNext())
				{
					//do nothing
					read.close();
					return;
				}
				//parse line as an expression
				parse(nonTerm, "expression");
				read.close();
			}
		}
		//if non-term is an expression
		else if(type.equals("expression"))
		{
			for(int i=0; i<nonTerm.length(); i++)
			{
				char cur =  nonTerm.charAt(i);
				//if current character is '('
				if(cur == '(')
				{
					//skip to its pair ')' and move on
					i = paranthesis(nonTerm, i);
				}
				//if current character is ')'
				else if(cur == ')')
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
				//if current character is '+'
				else if(cur == '+')
				{
					//parse left side of '+' as a term, right side of '+' including itself as moreterms
					parse(nonTerm.substring(0, i), "term");
					parse(nonTerm.substring(i), "moreterms");
					return;
				}
			}
			//no plus sign, then parse expression as a term
			parse(nonTerm, "term");
		}
		//if non-term is moreterms
		else if(type.equals("moreterms"))
		{
			//begins from index 1 because first term is '+'
			for(int i=1; i<nonTerm.length(); i++)
			{
				char cur =  nonTerm.charAt(i);
				if(cur == '(')
				{
					//skip to its pair ')' and move on
					i = paranthesis(nonTerm, i);
				}
				else if(cur == ')')
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}

				else if(cur == '+')
				{
					//parse between two '+'s as a term, add '+' to post-fix, and parse beginning current '+' to end as moreterms again
					parse(nonTerm.substring(1, i), "term");
					postfix.add("+");
					parse(nonTerm.substring(i), "moreterms");
					return;
				}
			}
			//no plus sign, then parse right side of '+' as a term and add '+' to post-fix
			parse(nonTerm.substring(1), "term");
			postfix.add("+");
		}
		//if non-term is a term
		else if(type.equals("term"))
		{
			for(int i=0; i<nonTerm.length(); i++)
			{
				char cur =  nonTerm.charAt(i);
				if(cur == '(')
				{
					//skip i to the index of pair ')'
					i = paranthesis(nonTerm, i);
				}
				else if(cur == ')')
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
				else if(cur == '*')
				{
					//parse left side of '*' as a factor and parse rest as morefactors
					parse(nonTerm.substring(0, i), "factor");
					parse(nonTerm.substring(i), "morefactors");
					return;
				}
			}
			//no '*' then parse the term as a factor
			parse(nonTerm, "factor");
		}
		//if non-term is a morefactors
		else if(type.equals("morefactors"))
		{
			//beginning from index 1 because there is '*' at index 0
			for(int i=1; i<nonTerm.length(); i++)
			{
				char cur =  nonTerm.charAt(i);
				if(cur == '(')
				{
					//skip i to the index of pair ')'
					i = paranthesis(nonTerm, i);
				}
				else if(cur == ')')
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
				else if(cur == '*')
				{
					//parse between two '*'s as a factor, add '*' to post-fix, and parse the rest as morefactors
					parse(nonTerm.substring(1, i), "factor");
					postfix.add("*");
					parse(nonTerm.substring(i), "morefactors");
					return;
				}
			}
			//no '*' then parse 
			parse(nonTerm.substring(1), "factor");
			postfix.add("*");
		}
		//if non-term is a factor
		else if(type.equals("factor"))
		{
			read = new Scanner(nonTerm);

			int numTokens = 0;
			//stores last token of the factor
			String last = "";
			while(read.hasNext())
			{
				numTokens++;
				last = read.next();
			}
			//if number of tokens in the factor is 0
			if(numTokens == 0)
			{
				System.out.println("Syntax error in line " + lineNum);
				System.exit(0);
			}
			//if number of tokens in the factor is one
			else if(numTokens == 1)
			{
				//if the token is a legit number or variable
				if(isNum(last) || isId(last))
				{
					//then add to postfix
					postfix.add(last);
				}
				//if the token is in parenthesis
				else if(last.charAt(0) == '(' && last.charAt(last.length()-1) == ')')
				{
					//then parse the expression between parenthesis
					parse(last.substring(1, last.length()-1), "expression");
				}
				//if the factor begins with "pow(", ends with ')' and contains ','
				else if(last.length() > 4 && last.substring(0,4).equals("pow(") && last.charAt(last.length()-1) == ')' && last.contains(","))
				{
					for(int i=4; i<last.length()-1; i++)
					{
						if(last.charAt(i) == '(')
						{
							//skip i to the index of pair ')'
							i = paranthesis(last, i);
						}
						else if(last.charAt(i) == ')')
						{
							System.out.println("Syntax error in line " + lineNum);
							System.exit(0);
						}
						else if(last.charAt(i) == ',')
						{
							/*parse expression between ',' and ')', then parse expression between
							'(' and ',' and then add "pow" to post-fix*/
							parse(last.substring(i+1, last.length()-1), "expression");
							parse(last.substring(4, i), "expression");
							postfix.add("pow");
							read.close();
							return;
						}
					}
				}
				else
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
			}
			//if number of tokens is greater than 1
			else
			{
				for(int i=0; i<nonTerm.length(); i++)
				{
					char cur = nonTerm.charAt(i);
					if(cur == ' ')
					{
						continue;
					}
					//if factor begins with '('
					else if(cur == '(')
					{
						for(int j=nonTerm.length()-1; j>i; j--)
						{
							if(nonTerm.charAt(j) == ' ')
							{
								continue;
							}
							//if factor ends with ')'
							else if(nonTerm.charAt(j) == ')')
							{
								//then parse expression between parenthesis
								parse(nonTerm.substring(i+1, j), "expression");
								read.close();
								return;
							}
							else
							{
								System.out.println("Syntax error in line " + lineNum);
								System.exit(0);
							}
						}
					}
					//if factor begins with "pow"
					else if(nonTerm.substring(i, i+3).equals("pow"))
					{
						for(int j=i+3; j<nonTerm.length(); j++)
						{
							if(nonTerm.charAt(j) == ' ')
							{
								continue;
							}
							//if '(' after "pow"
							else if(nonTerm.charAt(j) == '(')
							{
								for(int k=nonTerm.length()-1; k>j; k--)
								{
									if(nonTerm.charAt(k) == ' ')
									{
										continue;
									}
									//if factor ends with ')'
									else if(nonTerm.charAt(k) == ')')
									{
										//stores what inside the parenthesis of "pow()" is
										String powin = nonTerm.substring(j+1,k);
										if(powin.contains(","))
										{
											for(int l=0; l<powin.length(); l++)
											{
												if(powin.charAt(l) == '(')
												{
													//skip l to the index of pair ')'
													l = paranthesis(powin, l);
												}
												else if(powin.charAt(l) == ')')
												{
													System.out.println("Syntax error in line " + lineNum);
													System.exit(0);
												}
												else if(powin.charAt(l) == ',')
												{
													//parse expression between ',' and ')'
													parse(powin.substring(l+1, powin.length()), "expression");
													//parse expression between "pow(" and ','
													parse(powin.substring(0, l), "expression");
													//add "pow" to post-fix
													postfix.add("pow");
													read.close();
													return;
												}
											}
										}
										else
										{
											System.out.println("Syntax error in line " + lineNum);
											System.exit(0);
										}
									}
									else 
									{
										System.out.println("Syntax error in line " + lineNum);
										System.exit(0);
									}
								}
							}
							else
							{
								System.out.println("Syntax error in line " + lineNum);
								System.exit(0);
							}
						}
					}
					else
					{
						System.out.println("Syntax error in line " + lineNum);
						System.exit(0);
					}
				}
			}
		}
	}

	//checks if the string is a valid variable name
	static boolean isId(String token)
	{
		for(int i=0; i<token.length(); i++)
		{
			char cur = token.charAt(i);
			if(i==0 && !isLetter(cur))
			{
				return false;
			}
			else if(!isLetter(cur) && !isNum(cur))
			{
				return false;
			}
		}
		return true;
	}

	//checks if the character is a letter
	static boolean isLetter(char c)
	{
		return (c>='A' && c<='Z') || (c>='a' && c<='z'); 
	}
	
	//checks if the string is a valid number in hexadecimal
	static boolean isNum(String token)
	{
		for(int i=0; i<token.length(); i++)
		{
			if(!isHex(token.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}
	
	//checks if the character is a valid decimal digit
	static boolean isNum(char n)
	{
		return n>='0' && n<='9';
	}
	
	//skips index i to the parenthesis pair in the non-terminal String, detects if there is a parenthesis syntax error
	static int paranthesis(String nonTerm, int i)
	{
		i++;
		int counter = 1;
		for( ; i<nonTerm.length(); i++)
		{
			char cur = nonTerm.charAt(i);
			if(cur == '(')
			{
				counter++;
			}
			else if(cur == ')')
			{
				counter--;
				if(counter == 0)
				{
					return i;
				}
				else if(counter < 0)
				{
					System.out.println("Syntax error in line " + lineNum);
					System.exit(0);
				}
			}
		}
		System.out.println("Syntax error in line " + lineNum);
		System.exit(0);
		return -1;
	}
	
	//if the character is a valid hexadecimal digit
	static boolean isHex(char c)
	{
		return (c>='A' && c<='F') || (c>='a' && c<='f') || isNum(c);
	}
}