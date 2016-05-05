/**
 * Program: Assembler.java
 *
 * Purpose: This is our Assembler class. It takes the source from the editor 
 *          view and sends it through a two pass parser. Pass one parses the 
 *          text, splitting everything into labels, operations, and comments. 
 *          Pass two takes this information.
 *
 * @author: Jordan Lescallette
 * @author: Matthew Vertefeuille
 * @author: Guojun Liu
 *
 * date/ver: 
 */

/**
 * Change Log
 *
 * mv935583 -> Matthew Vertefeuille
 * jl948836 -> Jordan Lescallatte
 * # author   - date:     description
 * 01 jl948836 - 02/11/16: Comment character changes from ";" to "#"
 * 02 jl948836 - 02/18/16: Add Spaces back into String, that regEx parses out
 *                        Adjust String size for quotation (") characters
 * 03 jl948836 - 02/18/16: Added -1 to .length(), to parse off "]" at the end of
 *                        RBP, RSP register aliases
 * 
 * 04 mv935583 - 02/23/16: Fixed db so it does not append a null 
 *                                   character at the end
 * 05 mv935583 - 02/23/16: rload now requires hex offset to be 0x0N 
 *                                   instead of 0xN
 * 06 mv935583 - 02/23/16: jmpeq now works with R0 instead of just r0
 * 
 * 07 jl948836 - 02/28/16: made opcode local variable to all of getByteCode()
 * 
 * 08 jl948836 - 03/05/16: Comment Character overflow fixed; Error occurred when
 *                         comment character was the only character on a line
 *                         (excluding \n); check for array length to fix
 * 
 * 09 jl948836 - 03/05/16: Overload on return now does allows user full range of
 *                         0-Max for SP increment (got rid of "1" + ...)
 *  
 * 10 jl948836 - 03/14/16: Implemented EQU Pseudo-Op
 * 
 * 11 mv935583 - 03/18/16: Implemented functionality in order to invoke db on
 *                         a label
 * 
 * 12 mv935583 - 03/20/16: Implemented functionality in order to invoke db on
 *                         an EQU Pseudo-Op
 *
 * 13 jl948836 - 03/23/16: ret overload, now is user input n + 1
 * 
 * 14 jl948836 - 03/24/16: Changed byte code for ret Op-code with no argument;
 *                         Last byte is now 01 instead of 00
 * 
 * 15 jl948836 - 03/24/16: Get rid of "]" character so RBP and RSP translate to
 *                         RD and RE in RSTORE opcode
 * 
 * 16 jl948836 - 03/24/16: Took away excess nibble given to methods that do not
 *                         require the information
 * 
 * 17 jl948836 - 03/26/16: Added check to passTwo, to ensure EQU forward references
 *                         are resolved
 * 
 * 18 jl948836 - 03/26/16: Added Check to CALL to determine that EQU isn't a Register
 *                         RD and RE in RSTORE opcode
 * 
 * 19 jl948836 - 03/26/16: Added error statements to JMPLT and JMPEQ to check for
 *                         invalid characters in the argument
 * 
 * 20 gl939543 - 03/30/16: Modified DB sudoop 
 *
 * 21 gl939543 - 03/30/16  Add new condition to print out the DB contents in memory

 * 22 jl948836 - 04/01/16: Changed Byte Code of SRET from "63 00" to "63 01"
 * 
 * 22 mv935583 - 04/11/16: Implemented changed to add shift instructions
 * 
 * 20 jl948836 - 04/01/16: Changed Byte Code of SRET from "63 00" to "63 01"
 *
 * 22 jl948836 - 04/07/16: Corrected ByteCode Format of Operations
 * 
 * 23 jl948836 - 04/07/16: Swapped ByteCode of move and rload. rload is now 4
 *                         and move is D2. rload is now a 2 byte instruction.
 * 
 * 24 jl948836 - 04/10/16: Got rid of operationLocation(). No longer necessary
 *                         due to rload being the same size as all other instructions.
 * 
 * 25 jl948836 - 04/14/16: Error in getRegister(), changed .equls() to .matches() for
 *                         regEx
 *
 * 26 jl948836 - 04/14/16: isSingleHex() now accepts 0xH and 0x0H formats
 * 
 * 27 jl948836 - 04/15/16: bug, store now Handles EQU labels
 * 
 * 28 jl948836 - 04/15/16: bug, rload and rstore now handles 0xH and 0x0H formats 
 *                         for offset.
 *
 * 29 jl948836 - 04/10/16: Re-ordered the Instruction ByteCode methods to be in
 *                         ascending order of their ByteCodes.
 * 
 * 30 jl948836 - 04/15/16: Created aluOperations() to replace ADD, AND, OR, and XOR
 *                         functions. (generating Byte Code).
 * 
 * 31 jl948836 - 04/15/16: Created regAddFormat() to replace LOAD1 and LOAD2, as well
 *                         as chunks of JMPEQ, JMPLT, and STORE. (generating Byte Code)
 * 
 * 32 jl948836 - 04/16/16: Create bitManipFormat() to replace the ROR, ROL, SRA, SRL
 *                         SL functions. (generating byteCode).
 * 
 * 33 jl948836 - 04/16/16: Created directValueFormat() to replace the CALL, RET, SCALL
 *                         JMP functions. (generating byteCode).
 * 
 * 34 jl948836 - 04/16/16: Created dRegFormat() to replace the MOVE function. (generating
 *                         byteCode).
 * 
 * 35 jl948836 - 04/16/16: Created imDRegFormat() to replace chunks of RLOAD AND RSTORE
 *                         functions. (generating byteCode).
 * 
 * 36 mv935583 - 04/16/16: Created OPERATIONSMAP.
 * 
 * 37 jl948836 - 04/17-16: Re-wrote the generateByteCode() function to incorporate
 *                         changes 30-36.
 * 
 * 38 gl939543 - 04/19/16: Created cross reference under assembler listing
 * 
 * 39 gl939543 - 04/19/16: Point the error message to the error line in assembler listing if
 *                         error(s) detected. 
 * 
 * 40 jl948836 - 04/27/16: Added rules to what a label can be.
 * 
 * 41 jl948836 - 04/28/16: Modified isOperation() to use OPERATIONMAP
 * 
 * 42 jl948836 - 04/30/16: Removed invalid label from code list.
 * /

/*
* Change Log
* 
* -Guojun Liu
* -Modified the istore and rstore
* -Create a method to create assembler listing
*/

package machine.model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import machine.controller.MachineController;

public class Assembler {

    private final MachineController controller;
    private final ArrayList<String> byteCode = new ArrayList<>(); //contains generated Byte Code of user code
    private final ArrayList<String> codeList = new ArrayList<>(); //Contains unaltered user code
    private final static String[] PSEUDOOPS = {"SIP", "ORG", "BSS", "DB", "EQU"}; //CHANGE LOG: 10
    private static final Map<String, String> OPERATIONMAP; //CHANGE LOG BEING: 36
    static {
        OPERATIONMAP = new HashMap<>();
        OPERATIONMAP.put("LOAD", "2"); //Direct Load  RN,[XY]
        OPERATIONMAP.put("STORE", "3");
        OPERATIONMAP.put("MOVE", "D2");
        OPERATIONMAP.put("ADD", "5");
        OPERATIONMAP.put("CALL", "60");
        OPERATIONMAP.put("RET", "61");
        OPERATIONMAP.put("PUSH", "64");
        OPERATIONMAP.put("POP", "65");
        OPERATIONMAP.put("OR", "7");
        OPERATIONMAP.put("AND", "8");
        OPERATIONMAP.put("XOR", "9");
        OPERATIONMAP.put("ROR", "A0");
        OPERATIONMAP.put("JMPEQ", "B");
        OPERATIONMAP.put("JMP", "B0");
        OPERATIONMAP.put("HALT", "C000");
        OPERATIONMAP.put("ILOAD", "D0");
        OPERATIONMAP.put("ISTORE", "D1");
        OPERATIONMAP.put("RLOAD", "4");
        OPERATIONMAP.put("RSTORE", "E");
        OPERATIONMAP.put("JMPLT", "F");
        OPERATIONMAP.put("ROL", "A1");
        OPERATIONMAP.put("SRA", "A2");
        OPERATIONMAP.put("SRL", "A3");
        OPERATIONMAP.put("SL", "A4");
        OPERATIONMAP.put("SRET", "6301");
        OPERATIONMAP.put("SCALL", "62");
        OPERATIONMAP.put("NOOP", "0000");
    }
    //CHANGE LOG END: 36
    private final HashMap<String, Integer> labelMap; //labels mapped to addrs.
    private HashMap<String, String> equivalencies; //CHANGE LOG: 10 - labels mapped to labels/Registers
    private final HashMap<String, String> referenceLine;//line number(s) for referenced label
    private final HashMap<Integer, String> errorList;//line number(s) for referenced label
    private final HashMap<String, String> unresolveLtLReferences; //Marks all invalid Label to Label references
    private String[] codes, labels, tempMem; 
    private String Location[], Object_code[];
    private String DBcode = "";
    private String SIP = "00";
    private int codeLines = 1;
    private int labelAppearsLine;
    

    /**
     * Creates a new controller object and sets the memory table all to zeroes
     *
     * @param controller
     */
    public Assembler(MachineController controller) {
        this.controller = controller;
        this.labelMap = new HashMap<>(256);
        this.equivalencies = new HashMap<>(256);
        this.equivalencies = new HashMap<>(256);
        this.referenceLine = new HashMap<>(256);
        this.errorList = new HashMap<>(256);
        this.unresolveLtLReferences = new HashMap<>(256);

        // initialize tempMem
        tempMem = new String[256];
        for (int i = 0; i < 256; i++) {
            tempMem[i] = "00";
        }
    }

    /**
     * Entry point into this class - called by controller
     *
     * @param text  - source code from editor view
     * @return assembled byte code
     */
    public ArrayList<String> parse(String text) {
        passOne(text);
        passTwo();
       
        if (errorList.isEmpty()) {
            controller.setEditorErrorVisible(false);
        }
        else {
            displayErrors();
        }
        
        generateAssemblerList();  // Create an assembler list

        return byteCode;
    }

    /**
     * Displays the errors in the Editor whenever the user wants to compile
     * assembly code
     */
    private void displayErrors() {
        controller.setEditorErrors(errorList);
        controller.setEditorErrorVisible(true);
    }
    
    /**
     * 
     * @param text 
     */
    private void passOne(String text) {
        String[] lines = text.split("\n");
	int lineCount = lines.length;
	String[] label, tokens;
	codes = new String[lineCount];
        boolean validLabel;
        String op;

	stripComments(lines, lineCount);

	int currentLocation = 0;
        for (int i = 0; i < lineCount; i++) {
            if (codes[i].contains(":")) { //Line has a label
                label = codes[i].split(":.*"); //tokens[0] = label
                //Get rid of label, then split spaces to get tokens[0] = Mneumonic, tokens[1] = operand
                tokens = codes[i].replaceFirst(".+:\\s*", "").split("\\s+", 2);
                validLabel = isValidLabel(label[0]);
                if (validLabel && !codes[i].equals("")) { //label and operation/pseudo-op
                    if (isOperation(tokens[0])) {
                        labelMap.put(label[0], currentLocation);
                        referenceLine.put(label[0], "");
                        currentLocation += 2;
                    }
                    else if (isPseudoOp(tokens[0])) {
                        switch (tokens[0].toUpperCase()) {
                            case "BSS":
                                labelMap.put(label[0], currentLocation);
                                currentLocation += bssLocation(tokens, i);
                                break;
                            case "DB":
                                labelMap.put(label[0], currentLocation);
                                referenceLine.put(label[0], "");
                                currentLocation += dbOneLocation(tokens, i);
                                break;
                            case "EQU":
                                passOneEQU(label[0], tokens, i); //currentLocation unaffected
                                break;
                            default: //SIP, ORG
                                errorList.put((i+1), "Error: " + tokens[0].toUpperCase() + " on line " + (i+1) + " does not use a label.");
                                break;
                        }    
                    }
                    else if (validLabel) { //Label with nothing following
                        labelMap.put(label[0], currentLocation);
                        referenceLine.put(label[0], "");
                    }
                    else { //Invalid Operation/Pseudo-op/Missing Operand
                        errorList.put((i+1), "Error: " + tokens[0].toUpperCase() + " - Invalid Operation/Pseudo-Op on line " + (i+1));
                    }
                }
                else { //Invalid Label
                    errorList.put((i+1), "Error: Invalid label " + label[0] + " on line " + (i+1));
                }
            }
            else { //No label
                tokens = codes[i].split("\\s+", 2); //split line w/o label, on spaces
                System.out.println("             Tokens:" + Arrays.toString(tokens) + " Length: " + tokens.length);
                if (!codes[i].equals("") ){//Non-Blank Line
                    op = tokens[0].toUpperCase();
                    if (isOperation(tokens[0])) {
                        currentLocation += 2;
                    }
                    else if (isPseudoOp(tokens[0])) {
                        switch (tokens[0].toUpperCase()) {
                            case "ORG":
                                currentLocation = orgLocation(tokens, i);
                                break;
                            case "BSS":
                                currentLocation += bssLocation(tokens, i);
                                break;
                            case "DB":
                                currentLocation += dbOneLocation(tokens, i);
                                break;
                            case "EQU":
                                errorList.put((i+1), "Error: EQU missing a label on line " + (i+1));
                                break;
                            default: //SIP
                                break;
                        }
                    }
                    else { //Invalid Operation or Pseudo-Op
                        errorList.put((i+1), "Error: Invalid Operation/Pseudo-Op " + op + " on line " + (i+1));
                    }
                }
            }
	}
	resolveLtLReference();
    }
    
    /**
     * 
     */
    private void passTwo() {
        int currentLocation = 0;
        String[] tokens;
        String bytes;
        Location = new String[codes.length];
        Object_code = new String[codes.length];
        
        System.out.println("PassTwo");
        for (int i = 0; i < codes.length; i++) { 
            tokens = codes[i].replaceFirst(".+:\\s*", "").split("\\s+", 2);
            if (tokens.length >= 1 && !tokens[0].equals("")) {
                if (isPseudoOp(tokens[0].toUpperCase())) {
                    switch(tokens[0].toUpperCase()) { //if a Pseudo-Op
                        case "ORG":
                            currentLocation = orgLocation(tokens, i);
                            break;
                        case "DB":
                            Location[i] = intToHex(Integer.toString(currentLocation));
                            currentLocation += passTwoDB(tokens[1], currentLocation, i);
                            Object_code[i] = DBcode;
                            break;
                        case "BSS":
                            Location[i] = intToHex(Integer.toString(currentLocation));
                            currentLocation += bssLocation(tokens, i);
                            break;
                        case "SIP":
                            storeSIP(tokens, i);
                            break;
                        case "EQU":
                            break;
                        default: //Superfluous
                            break;
                    }
                }
                else if (isOperation(tokens[0])) {
                    bytes = generateByteCode(tokens, i + 1);
                    currentLocation += byteCodeInTemp(bytes, currentLocation, i);
                    //get Object Code for Assembler Listing
                    StringBuilder bytesInMemory = new StringBuilder(bytes);
                    if (bytesInMemory.length() > 5){
                        bytesInMemory.insert (2, " ").toString();
                        bytesInMemory.insert (5, " ").toString();
                        Object_code[i] = bytesInMemory.insert (8, " ").toString();
                    }
                    else{
                        Object_code[i] = bytesInMemory.insert (2, " ").toString();
                    }
                }
                else {
                    errorList.put(i+1, "Error: Some uncaught error on line " + (i+1));
                }
            }
        }

        byteCode.add(SIP);
        printContent();
        // build up bytecode for return
        byteCode.addAll(Arrays.asList(tempMem));
    }
    
    /**
     * Equivalent - Checks what kind of equivalency is being made (label to hex,
     * label to int, label to label, or label to register) and then makes the
     * reference and places them into the appropriate HashMap. (labelMap for
     * labels to int, and labels to hex. equivalencies for label to label, and
     * label to register.)
     * 
     * @param tokens - tokens[0] is the EQU Label, tokens[1] is EQU, 
     *                 tokens[2] is a Label, Register, Int, or Hex
     * @param i - location in the labels array
     */
    //CHANGE LOG BEGIN: 10
    private void passOneEQU(String label, String[] tokens, int i){
        if (tokens.length == 2){ //EQU, and Argument
            if (isHex(tokens[1])) { //Hex for an Argument
                labelMap.put(label, hexToInt(tokens[1]));
            }
            else if (isInt(tokens[1])){ //Int for an Argument
                labelMap.put(label, Integer.parseInt(tokens[1]));
            }
            else if (!equivalencies.containsKey(tokens[1])) { //Label or Register for an Argument
                equivalencies.put(label, tokens[1]);
            }
            else { //Label or Register for an Argument
                errorList.put((i+1), "Error: EQU Pseudo-Op on line " + (i+1) + " has an invalid argument.");
            }
        }
        else {
            String error = "Error: EQU pseudo op on line" + (i + 1) + " has to many arguments.";
            errorList.put((i+1), error);
        }
    }
    //CHANGE LOG END: 10

    /**
     * Ensures that the DB pseudo op argument conforms to standards and then returns
 length of the DB pseudo op argument.
     *
     * @param tokens String[] of pseudo ops
     * @return size of db pseudo op argument
     */
    private int passOneDB(String[] tokens) {
        int result;
        String temp = "";
        // concatenate the parameter to db
        for (int i = 1; i < tokens.length; i++) {
            temp += tokens[i];
            //CHANGE LOG BEGIN: 2
            if (i != tokens.length-1) { //re-add spaces that regex kills
                temp += " ";
            }
            //CHANGE LOG END: 2
        }
        // is the argument a string?
        if (temp.matches("[\"]{1}.*[\"]{1}") || temp.matches("[\']{1}.*[\']{1}")) {
            //-2 for both the beginning and ending " char. See passTwo
            result = temp.length() - 2; //CHANGE LOG: 2
            //System.out.println("In passOneDB, length of string is: " + result);
        } else { // not a string, split on ,
            String[] args = temp.split(",");
            result = args.length;
        }
        return result;
    }

    /**
     * Ensures that the BSS pseudo op argument conforms to standards and then returns
 length of the BSS pseudo op argument.
     *
     * @param value argument of BSS pseudo op
     * @return size of BSS pseudo op argument
     */
    private int passOneBSS(String value) {
        // we know string is valid hex or int
        if (isHex(value)) {
            return hexToInt(value);
        } else {
            return Integer.parseInt(value);
        }
        //TODO: Error Handling
    }

    /**
     * Determines if the address specified in ORG argument is valid hexadecimal
 equValue.
     * 
     * @param tokens String[] of pseudo ops
     * @param i location of ORG pseudo op in tokens
     * @return valid ORG argument as a integer
     */
    private int orgLocation(String[] tokens, int i) {
        int location = 0;
        if (tokens.length == 2 && isHex(tokens[1])) {
            location = hexToInt(tokens[1]);
        } else { // Invalid number of arguments or argument not hex
            errorList.put((i+1), "Error: invalid argument to ORG found on line " + (i + 1));
        }
        return location;
    }

    /**
     *
     * Ensures that the tokens array is of the appropriate length and then 
     * invokes passOneDb to find the number of bytes to skip in memory for 
     * memory storage of DB pseudo op.
     * 
     * @param tokens String[] of pseudo ops
     * @param i Location of DB pseudo op in tokens
     * @return Number of bytes to skip in memory
     */
    private int dbOneLocation(String[] tokens, int i) {
        int location = 0;
        if (tokens.length == 2) { // there must be an argument
            location = passOneDB(tokens);
        } else {
            errorList.put((i+1), "Error: Argument list for DB op is invalid on line " + (i + 1));
        }
        return location;
    }

    /**
     * Ensures that the tokens array is of the appropriate length and then 
     * invokes passOneBSS to find the number of bytes to skip in memory for 
     * memory storage of BSS pseudo op.
     * 
     * @param tokens String[] of pseudo ops
     * @param i Location of BSS pseudo op in tokens
     * @return Number of bytes to skip in memory
     */
    private int bssLocation(String[] tokens, int i) {
        int location = 0;
        if (tokens.length == 2 && (isHex(tokens[1]) || isInt(tokens[1]))) { // tokens[1] is hex or int
            location = passOneBSS(tokens[1]);
        } else {
            errorList.put((i+1), "Error: BSS pseudo-op on line " + (i + 1) + " invalid argument(s)");
        }
        return location;
    }

    /**
     *
     * @param tokens
     */
    private void storeSIP(String[] tokens, int i) {
        if (tokens.length == 2) {
            if (isHex(tokens[1])) {
                SIP = tokens[1].substring(2, 4);
            } 
            else if (isInt(tokens[1])) {
                SIP = intToHex((tokens[1]));
            } 
            else if (labelMap.containsKey(tokens[1])) {
                SIP = intToHex(labelMap.get(tokens[1]).toString());
            }
            //CHANGE LOG BEGIN: 10
            else if (equivalencies.containsKey(tokens[1])){
                String ref = equivalencies.get(tokens[1]);
                SIP = intToHex(labelMap.get(ref).toString());
            }
            //CHANGE LOG END: 10
        } else {
            errorList.put((i+1), "Error: SIP psuedo-op on line " + (i + 1) + "invalid argument");
        }
    }

    /**
     *
     * @param tokens
     * @param i
     * @return
     */
    private int dbTwoLocation(String dbString, int i, int currentLocation, int lineNum) {
        int location = 0;
        System.out.println(dbString);
        if (dbString.split("\\s+").length == 1) { // there must be an argument
            errorList.put((i+1), "Error: DB pseudo op argument list is invalid on line " + (i + 1));
        } else {
            location = passTwoDB(dbString, currentLocation, lineNum);
        }
        return location;
    }

    /**
     * handle instances of found DB pseudo op in passtwo.
     *
     * @param tokens
     * @return number of bytes to skip
     */
    private int passTwoDB(String dbString, int currentLocation, int lineNum) {
        System.out.println(dbString);
        int result = 0;
        //String temp = "";
        DBcode = "";
        //dbString = dbString.substring(3, dbString.length());

        String[] args = dbString.split(",\\s*");
        System.out.println(Arrays.toString(args));
        for (String arg : args) {
            System.out.println(arg + " " + currentLocation + " " + result);
            System.out.println(isHex(arg) + " " + arg);
            if (isHex(arg)) { 
                tempMem[currentLocation + result++] = Integer.toHexString(hexToInt(arg));
                System.out.println(result);
                DBcode += Integer.toHexString(hexToInt(arg)).toUpperCase() + " "; 
            } 
            else if (isInt(arg)) {
                tempMem[currentLocation + result++] = intToHex(arg);
                DBcode += intToHex(arg).toUpperCase() + " "; 
            } 
            else if (arg.matches("[\"]{1}[^\"]*[\"]{1}|[\']{1}[^\']*[\']{1}")) {
                int argLen = arg.length() - 1;
                for (int j = 0; j < argLen - 1; j++) {
                    tempMem[currentLocation + result++] = intToHex(Integer.toString((int) arg.charAt(j + 1)));
                    DBcode += intToHex(Integer.toString((int) arg.charAt(j + 1))).toUpperCase() + " "; 
                }
            }
            else {
                errorList.put((lineNum), "Invalid db parameter \"" + arg + "\" found on line " + lineNum);
            }
        }
        return result;
    }
    
    /**
     * Will remove in-line comments and comment lines from the code.
     * 
     * @param codeLines
     * @param lineCount
     */
    private void stripComments(String[] codeLines, int lineCount) {
        String[] tokens;
        
        for (int i = 0; i < lineCount; i++) {
            codeList.add(codeLines[i]);
            // CHANGE LOG BEGIN: 1
            tokens = codeLines[i].split("#");
            //if entireLine != a comment && entireLine more than comment character
            if (!codeLines[i].trim().equals("#") && tokens.length != 0){ //CHANGE LOG: 8
                codes[i] = tokens[0].trim(); //put code in codes
            }
            //CHANGE LOG END: 1
            else{ //no code
                codes[i] = "";
            }
        }
    }
    
    /**
     * Makes sure that all EQU labels map to existing labels.
     */
    private void resolveLtLReference() {
        String equValue;
        for (String equKey : equivalencies.keySet()) {
            equValue = equivalencies.get(equKey);
            System.out.println("EQUKEY: " + equKey + " EQUVALUE: " + equValue);
            if (!labelMap.containsKey(equValue) && !equValue.matches("[Rr][0-9A-Fa-f]|RSP|RBP")) {
                unresolveLtLReferences.put(equKey, equValue);
            }
        }
    }

    /**
     * Takes an the the mnemonic tokens and converts it into its byteCode format.
     * 
     * @param operation - Operation Name and Operands
     * @param line - Line number of the Operation
     * @return - A String equValue containing the Op-Code of the tokens
     */
    private String generateByteCode(String[] tokens, int line) {
        //String[] tokens = tokens.split("\\s+", 2); //split opcode from args
        String op = tokens[0].toUpperCase();
        
        if (isOperation(op)) { //valid Operation
    
            String opCode = OPERATIONMAP.get(op);
            
            if (tokens.length == 2) { //Op-Code has arguments
                
                String[] args = tokens[1].split("\\s*,\\s*");
                String[] tempArgs = args;  //make sure check referenced label will not change anything in args
                checkReferencedLabel(tempArgs, line);  //check if the args contains reference label
                if (args.length == 1) { //1 Argument
                    //CALL, SCALL, JMP
                    if (opCode.matches("60|62|B0")) {
                        return opCode + imValFormat(op, args[0], line);
                    }//end if
                    //RET
                    else if (opCode.matches("61")) {
                        return opCode + retSyntax(op, args[0], line);
                    }
                    //PUSH, POP
                    else if (opCode.matches("64|65")) {
                        return opCode + sRegFormat(op, args[0], line);
                    }//end else if
                }//end if
                else if (args.length == 2) { //2 Arguments
                    //ROR, ROL, SRA, SRL, SL
                    if (opCode.matches("A0|A1|A2|A3|A4")) {
                        return opCode + regRedImFormat(op, args[0], args[1], line);
                    }//end if
                    //JMPEQ, JMPLT
                    else if (opCode.matches("B|F")) {
                        return opCode + jmpCompSyntax(op, args[0], args[1], line);
                    }//end else if
                    //MOVE
                    else if (opCode.matches("D2")) {
                        return opCode + dRegFormat(op, args[0], args[1], line);
                    }//end else if
                    else {
                        switch (opCode) {
                            case "D0": //ILOAD
                                return opCode + iloadSyntax(op, args[0], args[1], line);
                            case "D1": //ISTORE
                                return opCode + istoreSyntax(op, args[0], args[1], line);
                            case "3": //STORE
                                return opCode + storeSyntax(op, args[0], args[1],line);
                            case "4": //RLOAD
                                return opCode + relativeSyntax(op, args[0], args[1], line);
                            case "E": //RSTORE
                                return opCode + relativeSyntax(op, args[0], args[1], line);
                            case "2": //Direct + Indirect Load
                                return loadSyntax(op, args[0], args[1], line);
                        }//end switch
                    }//end else
                }//end else if
                else if (args.length == 3) { //3 Arguments
                    //ADD, AND, OR, XOR
                    if (opCode.matches("5|7|8|9")) {
                        return opCode + triRegFormat(op, args[0], args[1], args[2], line);
                    }//end if
                }//end else if
                else { //To many Arguments
                    errorList.put((line), "Error: To many arguments on line " + line);
                }//end else
                    
            }//end if
            else { //No Arguments
                //NO-OP, HALT, RET, SRET
                if (OPERATIONMAP.get(op).matches("0000|C000|6301")) {
                    return OPERATIONMAP.get(op);
                }//end if
                else if (OPERATIONMAP.get(op).matches("61")) {
                    return OPERATIONMAP.get(op) + "01";
                }//end else if
                else { //Valid Op with Missing Arguments
                    errorList.put((line), "Error: Missing Arguments for " + op + " on line " + line);
                }//end else
            }//end else
        }//end if
        else { //Invalid Operation
            errorList.put((line), "Error: Invalid Operation " + op + " on line " + line);
        }//end else
                    return "0000";
    }
    
    /**
     *
     * @param bytes
     * @param currentLocation
     * @param i
     * @return
     */
    private int byteCodeInTemp(String bytes, int currentLocation, int i) {
        int location;
            tempMem[currentLocation] = bytes.substring(0, 2);// Placing Bytecode in memory
            tempMem[currentLocation + 1] = bytes.substring(2, 4);
            System.out.println("Code: " + codes[i] + "Currentlocation: " + intToHex(Integer.toString(currentLocation)));
            Location[i] = intToHex(Integer.toString(currentLocation));
            
            location = 2;
        /*}*/
        return location;
    }

    /**
     * Single Register Format - Generate the second byte of the PUSH(64) and POP(65) 
     * Op-Codes. The Last nibble is always 0 (unused).
     * 
     * @param op - PUSH, POP
     * @param firstArg - Register
     * @param line - Line number of the Op-Code
     * @return - the second byte of the Op-Code
     */
    private String sRegFormat(String op, String firstArg, int line) {
        return getRegister(op, firstArg, line) + "0";
    }
    
    /**
     * Double Register Format - Generates the second byte of the MOVE(D2) Op-Code.
     * 
     * @param op - MOVE
     * @param firstArg - Destination Register
     * @param secondArg - Source Register
     * @param line - Line Number of the Op-Code
     * @return - the second byte of the Op-Code
     */
    //CHANGE LOG: 22, 34
    private String dRegFormat(String op, String firstArg, String secondArg, int line) {
        return getRegister(op, firstArg, line) + getRegister(op, secondArg, line);
    }
    
    /**
     * Triple Register Format - Generates the bottom three nibbles of the ADD(5), AND(8), 
     * OR(7), and XOR(9) Op-Codes.
     * 
     * @param firstArg - Destination Register
     * @param secondArg - Source Register 1
     * @param thirdArg - Source Register 2
     * @param line - Line number of the Op-Code
     * @return - the bottom three nibbles of the Op-Code
     */
    //CHANGE LOG BEGIN: 30
    private String triRegFormat(String op, String firstArg, String secondArg, String thirdArg, int line) {
        return getRegister(op, firstArg, line) + getRegister(op, secondArg, line) + getRegister(op, thirdArg, line);
    }
    //CHANGE LOG END: 30
    
    /**
     * Register Immediate Format - Generates the bottom three nibbles of the STORE, 
     * JMPEQ, JMPLT, direct LOAD, and immediate LOAD instructions. Dereferences 
     * labels and EQU labels to obtain the address.
     * 
     * @param op - LOAD, STORE, JMPEQ, JMPLT
     * @param firstArg - Register
     * @param secondArg - Address
     * @param line - Line number of the Op-Code
     * @return - Bottom three nibbles of the Op-Code
     */
    //CHANGE LOG BEING: 31
    private String regImFormat(String op, String firstArg, String secondArg, int line) {
        String result = "000";
        String register = getRegister(op, firstArg, line);
        String address = secondArg;
        System.out.println("regImFormat: " + address);
        if (labelMap.containsKey(address)) {
            result = register + intToHex(Integer.toString(labelMap.get(address)));
        }
        else if (equivalencies.containsKey(address)) {
            if (unresolveLtLReferences.containsKey(address)) {
                errorList.put(line, "Error: " + address + " - forward Reference - " + 
                        unresolveLtLReferences.get(address) + " - does not exist. Referenced on line " + line);
            }
            else {
                String ref = equivalencies.get(address);
                result = register + intToHex(Integer.toString(labelMap.get(ref)));
            }
        }
        else if (isHex(address)) {
            result = register + address.substring(2, 4);
        } 
        else if (isInt(address)) {
            result = register + intToHex(address);
        } 
        else {
            errorList.put((line), "Error: " + op + " operation on line " + line + 
                    " has invalid arguments.");
        }
        
        return result;
    }
    //CHANGE LOG END: 31
    
    /**
     * Register Reduced Immediate Format - Generates the bottom Byte of the ROR(A0), ROL(A1), 
     * SRA(A2), SRL(A3), and SL(A4) Op-Codes. Immediate equValue limited to a single nibble
     * (usable range 0 <= n < 9).
     * 
     * @param op - ROR, ROL, SRA, SRL, SL
     * @param firstArg - Destination/Source Register 
     * @param secondArg - Number of Bits to manipulate (range 0 <= n < 9)
     * @param line - Line number of the tokens
     * @return - the bottom Byte of the Op-Code 
     */
    //CHANGE LOG BEGIN: 32
    private String regRedImFormat(String op, String firstArg, String secondArg, int line) {
        String result = "00";
        
        boolean errorFlag = false;
        int secondArgDecFormat;
        firstArg = getRegister(op, firstArg, line);
        if (secondArg.length() == 1 || secondArg.length() == 4) {
            if (isHex(secondArg)) {
                secondArgDecFormat = Integer.parseInt(secondArg.substring(3, 4), 16);
                if (0 <= secondArgDecFormat && secondArgDecFormat < 9) {
                    result = firstArg + secondArg.substring(3, 4);
                }
                else {
                    errorFlag = true;
                }
            }
            else if (isInt(secondArg)) {
                secondArgDecFormat = Integer.parseInt(secondArg, 10);
                if (0 <= secondArgDecFormat && secondArgDecFormat < 9) {
                    result = firstArg + secondArg;
                }
                else {
                    errorFlag = true;
                }
            }
            else if (labelMap.containsKey(secondArg)) {
                result = firstArg + intToHex(Integer.toString(labelMap.get(secondArg)));
            }
            else if (equivalencies.containsKey(secondArg)) {
                String ref = equivalencies.get(secondArg);
                result = firstArg + intToHex(Integer.toString(labelMap.get(ref)));
            }
            else {
                    errorFlag = true;
                }
            
        }
        if (errorFlag) {
            errorList.put((line), "Error: " + op + " operation on line " + line +
                            " has invalid arguments.");
        }
        
        return result;
    }
    //CHANGE LOG END: 32

    /**
     * Immediate Double Register Format - generates the bottom three nibbles of
     * the RLOAD(4) and RSTORE(E) Op-Codes.
     * 
     * @param op - RLOAD, RSTORE
     * @param offset - Offset from the Address in the Register (range -8 <= H <= 7)
     * @param firstArg - Register
     * @param secondArg - Register
     * @param line - Line Number of the Op-Code
     * @return - bottom three nibbles of the Op-Code
     */
    //CHANGE LOG BEGIN: 35
    private String imDRegFormat(String op, String offset, String firstArg, String secondArg, int line) {
        String result = "000";
        boolean errorFlag = false;
        //Handle Equivalencies
        if (labelMap.containsKey(offset)) {
            offset = "0x" + intToHex(Integer.toString(labelMap.get(offset)));
        }
        else if (equivalencies.containsKey(offset)) {
            String ref = equivalencies.get(offset);
            offset = "0x" + intToHex(Integer.toString(labelMap.get(ref)));
        }
        
        //Handle offset (including the equValue dereferenced from Equivalencies
        if (isSingleHex(offset)) {
            if (offset.length() == 3){
                offset = offset.toUpperCase().substring(2, 3);
            }
            else if (offset.length() == 4){
                offset = offset.toUpperCase().substring(3, 4);
            }
        }
        else if (offset.startsWith("-") && offset.length() == 2) {
            int number = Integer.parseInt(offset.toUpperCase().substring(1, 2));
            switch (number) {
                case 1:
                    offset = "F";
                    break;
                case 2:
                    offset = "E";
                    break;
                case 3:
                    offset = "D";
                    break;
                case 4:
                    offset = "C";
                    break;
                case 5:
                    offset = "B";
                    break;
                case 6:
                    offset = "A";
                    break;
                case 7:
                    offset = "9";
                    break;
                case 8:
                    offset = "8";
                    break;
                default:
                    errorFlag = true;

            }
        }
        else if (offset.length() == 1 && Integer.parseInt(offset) < 8) {
            //Do Nothing
        }
        else {
            errorFlag = true;
        }
        
        if (errorFlag) {
            errorList.put((line), "Error: Invalid offset for " + op + " found on line " + line);
            return result;
        }
        
        result = offset + getRegister(op, firstArg, line) + getRegister(op, secondArg, line);
        return result;
        
    }
    //CHANGE LOG END: 35
    
    /**
     * Immediate Value Format - Generates the bottom byte of the CALL(60), RET(61), 
     * SCALL(62), and JMP(B0) Op-Codes.
     * 
     * @param op - CALL, RET, SCALL, JMP
     * @param firstArg - Address/Label
     * @param line - Line number of the tokens
     * @return - the second byte of the Op-Code 
     */
    //CHANGE LOG BEGIN: 33
    private String imValFormat(String op, String firstArg, int line) {
        String result = "00";
        
        if (labelMap.containsKey(firstArg)) { //arg is a label
            result = intToHex(Integer.toString(labelMap.get(firstArg)));
        }
        else if (equivalencies.containsKey(firstArg)) { //arg is a label-to-label EQU
            String ref = equivalencies.get(firstArg);
            if (!ref.toUpperCase().matches("R[0-9A-F]|RSP|RBP")) { //Not a Register
                result = intToHex(Integer.toString(labelMap.get(ref)));
            }
            else {
                errorList.put((line), "Error: Invalid Destination for " + op + " on line " + line);
            }
        }
        else if (isInt(firstArg)) { // arg is decimal
            result = intToHex(firstArg);
        } 
        else if (isHex(firstArg)) { // arg is hex
            result = firstArg.substring(2, 4);
        }
        else {
            errorList.put((line), "Error: Invalid destination for " + op + " on line " + line);
        }
        
        return result;
    }
    //CHANGE LOG END: 33
    
    /**
     * Load Syntax - Handles Syntax Checking for Direct Loads(1) and Immediate loads(2).
     * Generates the entire Op-Code.
     * 
     * @param op - LOAD
     * @param firstArg - Destination Register
     * @param secondArg - int/hex/label with or w/o "[]"
     * @param line - Line number of the Op-Code
     * @return - The entire two bytes of the Op-Code
     */
    private String loadSyntax(String op, String firstArg, String secondArg, int line) {
        String result;
        String[] arg;
        if (secondArg.matches("\\[.+\\]")) { //Direct Load
            arg = secondArg.split("\\[|\\]");
            result = "1" + regImFormat(op, firstArg, arg[1], line);
        }
        else { //Immediate Load
            result = "2" + regImFormat(op, firstArg, secondArg, line);
        }
        
        return result;
    }
    
    /**
     * Store Syntax - Handles the syntax checking for STORE(3). 
     * 
     * @param op - STORE
     * @param firstArg - int/hex/label with "[]"
     * @param secondArg - Source Register
     * @param line - Line number of the Op-Code
     * @return - The bottom three nibbles for the Op-Code
     */
    //modified storeSyntax  --- the storeSyntax command is: STORE [XY], RN
    //store the equValue in register N in the memory cell at address XY
    private String storeSyntax(String op, String firstArg, String secondArg, int line) {
        String result = "000";
        if (firstArg.matches("\\[.+\\]")){
            firstArg = firstArg.substring(1, firstArg.length() - 1);
            //CHANGE LOG: 31
            result = regImFormat(op, secondArg, firstArg, line);
        }
        else {
            errorList.put((line), "Error: Invalid Syntax for " + op + " on line " + line);
        } 
        return result;
    }
    /*
     * Relative Syntax - Handles the syntax checking for RLOAD(4) and RSTORE(E).
     * 
     * @param op - RLOAD, RSTORE
     * @param firstArg - Destination Register
     * @param secondArg - Source Register
     * @param line - Line number of the Op-Code
     * @return Assembled byte values for the Op-Code.
     */
    //CHANGE LOG BEGIN: 38
    private String relativeSyntax(String op, String firstArg, String secondArg, int line) {
        String result = "000"; //CHANGE LOG: 21
        String tokens[];
        if (secondArg.matches(".+\\[.+\\]")){
            tokens = secondArg.split("\\[|\\]"); //tokens[0]=offset, tokens[1]= reg]
            result = imDRegFormat(op, tokens[0], firstArg, tokens[1], line);
            return result;
        }
        else if (firstArg.matches(".+\\[.+\\]")) {
           tokens = firstArg.split("\\[|\\]");
           System.out.println(Arrays.toString(tokens));
           result = imDRegFormat(op, tokens[0], tokens[1], secondArg, line);
           return result;
        }
        else {
            errorList.put((line), "Error: Invalid Syntax for " + op + " on line " + line);
            return result;
        }
    }
    //CHANGE LOG END: 38
    
    /**
     * 
     * @param op
     * @param firstArg
     * @param line
     * @return 
     */
    private String retSyntax(String op, String firstArg, int line) {
        String result = "01";
        
        if (isInt(firstArg)) {
            result = Integer.toString(Integer.parseInt(firstArg) + 1);
            result = intToHex(result);
        }
        else if (isHex(firstArg)) {
            result = Integer.toString(hexToInt(firstArg) + 1);
            result = result.substring(2, 4);
        }
        else if (equivalencies.containsKey(firstArg)) {
            String ref = equivalencies.get(firstArg);
            result = intToHex(Integer.toString(labelMap.get(ref) + 1));
        }
        else {
            errorList.put((line), "Error: Invalid Syntax for " + op + " on line " + line);
        }
        
        return result;
    }
    
    /**
     * Jump Comparison Syntax - Test to see whether the Op-Code is JMPEQ or JMPLT, 
     * parse the syntax and call the Register Immediate Format function.
     * 
     * @param op - JMPEQ, JMPLT
     * @param firstArg - Reg=Reg or Reg<Reg (Reg can be a EQU lable)
     * @param secondArg - Label/Address
     * @param line - Line number of the Op-Code
     * @return - The bottom three nibbles of the Op-Code
     */
    private String jmpCompSyntax(String op, String firstArg, String secondArg, int line) {
        String result = "000";
        //CHANGE LOG BEGIN: 10
        if (firstArg.matches(".+=.+|.+<.+")) {
            String[] first = firstArg.split("=|<");
            if (isComparisonReg(first[1])) {
                result = regImFormat(op, first[0], secondArg, line);
            }
            else {
                errorList.put((line), "Invalid Operator for " + op + " on line " + line);
            }
        }            
        return result;
    }
    
    /**
     * Indirect Load Syntax - Handles the syntax checking for ILOAD(D0).
     * 
     * @param op - ILOAD
     * @param firstArg - Destination Register
     * @param secondArg - [Source Register]
     * @param line - Line number of the Op-Code
     * @return - the second byte for the Op-Code
     */
    private String iloadSyntax(String op, String firstArg, String secondArg, int line) {
        String result = "00";
        String[] tokens;
        if (secondArg.matches("\\[.+\\]")) {
            tokens = secondArg.split("\\[|\\]");
            result = dRegFormat(op, firstArg, tokens[1], line); //CHANGE LOG: 34
        } 
        else {
            errorList.put((line), "Error: ILOAD operation on line " + line
                + " has invalid arguments.");
        }
        return result;
    }
    
    /**
     * Indirect Store Syntax - Handles the syntax checking for ISTORE(D1).
     * 
     * @param op - ISTORE
     * @param firstArg - [Destination Register]
     * @param secondArg - Source Register
     * @param line - Line number of the Op-Code
     * @return - The second byte for the Op-Code
     */
    //modified ISTORE: ISTORE [RM], RN
    //store the equValue in register N into the memory cell referenced by the 
    //address in register M
    private String istoreSyntax(String op, String firstArg, String secondArg, int line) {
        String result = "00";
        String[] tokens;
        if (firstArg.matches("\\[.+\\]")) {
            tokens = firstArg.split("\\[|\\]");
            result = dRegFormat(op, tokens[1], secondArg, line); //CHANGE LOG: 34
        } 
        else {
            errorList.put((line), "Error: ISTORE operation on line " + line
                + " has invalid arguments.");
        }
        return result;
    }
    
    /**
     * Helper method Returns a string with the corresponding register
     *
     * @param register
     * @return The index of the specified register
     */
    private String getRegister(String op, String register, int line) {
        //CHANGE LOG BEGIN: 10
        if (equivalencies.containsKey(register)){
            if (equivalencies.get(register).toUpperCase().matches("R[0-9A-F]|RSP|RBP")){
                register = equivalencies.get(register).toUpperCase();
                if (register.toUpperCase().equals("RBP")) { //CHANGE LOG: 14
                    return "D";
                } else if (register.toUpperCase().equals("RSP")) { //CHANGE LOG: 14
                    return "E";
                }
                return register.substring(1);
            }
        }
        //CHANGE LOG END: 10
        else if (register.toUpperCase().matches("R[0-9A-F]|RSP|RBP")) {
            /*
             * Added the or statements to allow for users to call
             * either the literal name of the bp or just the 
             * register name
             * Cody Galbreath - 03/23/2014
             */
            if (register.toUpperCase().equals("RBP"))
                return "D";
            else if (register.toUpperCase().equals("RSP"))
                return "E";
            
            return register.substring(1);
        }       
        errorList.put((line), "Error: Invalid register for " + op + " on line " + line);
        return "0";
    }
    /**
     * 
     * @param register
     * @param line
     * @return 
     */
    //CHANGE LOG BEGIN: 10
    private boolean isComparisonReg(String register) {
        if ("R0".equals(register.toUpperCase())){
            return true;
        }
        else if (equivalencies.containsKey(register) && "R0".equals(equivalencies.get(register).toUpperCase())){
            return true;
        }
        
        return false;
        
    }//end getComparisonReg()
    //CHANGE LOG END: 10
    
    /**
     * Helper Method to ensure that a label is both unique and follows the valid
     * label format (underscores and alpha-numeric characters). 
     * Invalid Characters: Spaces, only numeric, only hex values, register names
                     and tokens mnemonics.
     *
     * @param labels
     * @param token
     * @return boolean
     */
    private boolean isValidLabel(String token) {
        if (token.matches("[a-zA-Z0-9_]*")) {
            if (token.matches("[0-9]*|0[xX0-9a-fA-F]*|[rR][0-9a-fA-F]")
                    || token.toUpperCase().matches("RBP|RSP") || isOperation(token) || isPseudoOp(token)) {
                return false;
            }
            //for (String label : labels) {
            for (String label : labelMap.keySet()) {
                //if label is already used, or is an tokens or pseudo-op mnemonic
                if (label != null && label.equals(token)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Helper method to check for valid 2 digit int string
     *
     * @param number
     * @return boolean
     */
    private boolean isInt(String number) {
        if (number.length() == 0 || number.length() > 4) {
            return false;
        } 
        else if (number.matches("[0-9][0-9]{0,2}") || number.matches("-[0-9][0-9]{0,2}")) {
            return true;
        }
        return false;
    }

    /**
     * Test to see if the user input a single Hex character in either 0xH or
     * 0x0H format.
     * 
     * @param number
     * @return True if it is Hex, false if not
     */
    private boolean isSingleHex(String number) {
        //System.out.println(number.length());
        //CHANGE LOG BEGIN: 26
        if (number.length() == 3){
            if (number.substring(0,2).equalsIgnoreCase("0x") &&
                    number.substring(2,3).toUpperCase().matches("[0-9A-F]")) {
                return true;
            }
        } 
        //CHANGE LOG END: 26
        else if (number.length() == 4) {
            if (number.substring(0, 3).equalsIgnoreCase("0x0")
                && number.substring(3, 4).toUpperCase().matches("[0-9A-F]")) {  //CHANGE LOG END: 5
                return true;
            }
        }
        return false;
    }
 
    /**
     * Helper method to check for valid 2 digit hex string must have '0x' at the
     * start to be valid
     *
     * @param number
     * @return boolean
     */
    private boolean isHex(String number) {
        int len = number.length();
        if (len > 4 || len < 3) {
            return false;
        } else {
            if (len == 4) {
                if (number.substring(0, 2).equalsIgnoreCase("0x")
                    && number.substring(2, 4).toUpperCase().matches("[0-9A-F]{2}")) {
                    return true;
                }
            } else {
                if (number.substring(0, 2).equalsIgnoreCase("0x")
                    && number.substring(2, 3).toUpperCase().matches("[0-9A-F]")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method, hex string to integer, chops 0x at start of string
     *
     * @param hex
     * @return integer
     */
    private int hexToInt(String hex) {
        return Integer.parseInt(hex.toUpperCase().replaceFirst("0X", ""), 16);
    }

    /**
     * Helper method, decimal string to hex string. 0x is left off.
     *
     * @param decimal
     * @return hexadecimal
     */
    private String intToHex(String decimal) {
        String result = Integer.toHexString(Integer.parseInt(decimal)).toUpperCase();
        if (result.length() == 1) {
            result = "0" + result;
        } 
        else if (result.length() > 2) {
            result = result.substring(result.length() - 2);
        }
        return result;
    }

    /**
     * loop through PSEUDOOPS and see if token is valid
     *
     * @param token
     * @return boolean
     */
    private boolean isPseudoOp(String token) {
        for (String pseudoOp : PSEUDOOPS) {
            if (pseudoOp.equals(token.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * loop through OPERATIONS and see if token is valid
     *
     * @param token
     * @return boolean
     */
    private boolean isOperation(String token) {
        return OPERATIONMAP.containsKey(token.toUpperCase()); //CHANGE LOG: 41        
    }

    /**
     * Prints the contents of the labels array and the codes array for the
     * virtual machine.
     *
     * @param labels
     * @param codes
     */
    public void printContent() {        
        System.out.println("\nPrinting through Label map");
        for (String key: labelMap.keySet()) {
            System.out.println(key + " : " + intToHex(Integer.toString(labelMap.get(key))));
        }
        
        //CHANGE LOG BEGIN: 10
        System.out.println("\nPrintin through Equivalencies Map");
        for (String key: equivalencies.keySet()){
            System.out.println(key + " : " + equivalencies.get(key));
        }
        //CHANGE LOG END: 10
        
        System.out.println("\nCODES:");
        for (String code : codes) {
            System.out.println(code);
            //codeList.add(codes[i]);
        }
        
        for (int i = 0; i < tempMem.length; i++) {
            if ((i+1) % 16 == 0) {
                System.out.print(tempMem[i]);
                System.out.println();              
            } else {
                System.out.print(tempMem[i] + ", ");
            
            }
        }
    }
    
    /**
     * Author: Guojun Liu
     * 04/19/2016 
     * @param operation
     * @param line 
     * Check if the give line's code contains referenced label
     */
    
    private void checkReferencedLabel(String[] checkargs, int line){
        String lineNum = Integer.toString(line);
        if (checkargs.length == 1){  //1 Argument
            if (referenceLine.containsKey(checkargs[0])){
                referenceLine.put(checkargs[0], referenceLine.get(checkargs[0])+lineNum + "  ");
            }
        }//end only 1 argument check
        else if (checkargs.length == 2){ //2 Arguments
            //First two if conditions hold the situations for when "STORE", "RLOAD", "RLOAD", "RSTORE" etc. using references.
            if (checkargs[0].startsWith("[")){                          
                checkargs[0] = checkargs[0].replaceAll("[\\[.\\]]","");
                updateReferenceWithTwoArgs(checkargs, lineNum);
                checkargs[0] = "[" + checkargs[0] + "]";
            }
            else if(checkargs[1].startsWith("[")){
                checkargs[1] = checkargs[1].replaceAll("[\\[.\\]]","");
                updateReferenceWithTwoArgs(checkargs, lineNum);
                checkargs[1] = "[" + checkargs[1] + "]";
            }
            else{
                updateReferenceWithTwoArgs(checkargs, lineNum);
            }           
        }//end 2 arguments check
        else if(checkargs.length == 3){ //3 Arguments
            if (referenceLine.containsKey(checkargs[0])){
                referenceLine.put(checkargs[0], referenceLine.get(checkargs[0])+lineNum + "  ");
            }
            if (referenceLine.containsKey(checkargs[1])){
                referenceLine.put(checkargs[1], referenceLine.get(checkargs[1])+lineNum + "  ");
            }
            if (referenceLine.containsKey(checkargs[2])){
                referenceLine.put(checkargs[2], referenceLine.get(checkargs[2])+lineNum + "  ");
            }
        }//end 3 arguments check
    }
    
    /**
     * Author: Guojun Liu
     * 04/19/2016 
     * @param newArgs
     * @param line 
     */
    private void updateReferenceWithTwoArgs (String[] newArgs, String line){
        if (referenceLine.containsKey(newArgs[0])){
            referenceLine.put(newArgs[0], referenceLine.get(newArgs[0])+ line + "  ");
        }
        if (referenceLine.containsKey(newArgs[1])){
            referenceLine.put(newArgs[1], referenceLine.get(newArgs[1])+line + "  ");
        }
    }

    /*
    *   Author: Guojun Liu
    *   03/15/2016
    *   Create a assembler list 
    */
      
    private void generateAssemblerList(){
        String restDBcode = "";             //Declare a string DB contents in the memory
        Date date = new Date();
        SimpleDateFormat simpDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        java.io.File file = new java.io.File("WALL-Listing.lst");
        try (java.io.PrintWriter output = new java.io.PrintWriter(file)) {
            output.println("*************************** WALL-Listing ***********************");
            output.println("Created date: " + simpDate.format(date));
            output.println("\n");
            output.println("Location    " + "Object Code       " + "Line   " + "Source Statement");
            for (int i = 0; i<codeList.size(); i++){
                if(errorList.get(i) != null){     // If code line contains error, dispaly the error
                    output.println("=========================>" + errorList.get(i));
                }
                if ( Location[i] != null){
                    if (Object_code[i] != null){
                        if (Object_code[i].endsWith("]")){
                            Object_code[i] = Object_code[i].substring(0, Object_code[i].length()-1);
                        }
                        if (Object_code[i].length() > 15){     
                            String tempDBcode = Object_code[i].substring(0, 15);
                            output.printf("%-12s%-18s%4d%4s", Location[i], tempDBcode, codeLines, " ");
                            restDBcode  = Object_code[i].replace(tempDBcode, "");     //Remove first row of DB content
                        }else{
                            output.printf("%-12s%-18s%4d%4s", Location[i], Object_code[i], codeLines, " ");
                        }                       
                    }else{
                        output.printf("%-12s%-18s%4d%4s", Location[i], " ", codeLines, " ");
                    }
                }else{
                    output.printf("            " + "                  " + "%4d" + "   ", codeLines);
                }
                output.println(codeList.get(i));
                //This while loop invoks only when DB is very long
                while(restDBcode.length() > 0){
                    if (restDBcode.length() > 15){
                        String tempDBcode = restDBcode.substring(0, 15);
                        output.printf("%-12s%-18s%4s%4s", " ", tempDBcode, " ", " ");
                        output.println();
                        restDBcode  = restDBcode.replace(tempDBcode, "");     //Remove first row of DB content
                    }else{
                        //String tempDBcode = restDBcode;
                        output.printf("%-12s%-18s%4s%4s", " ", restDBcode, " ", " ");
                        output.println();
                        restDBcode  = "";                                   //Clear restDBcode
                    }
                }//end while
                             
                codeLines++;
            }
            
            //The following part will generate a cross reference listing for the above assembler listing.
            output.println();
            output.println();
            output.println("***************** WALL Cross-Reference Listing *******************");
            output.println("Created date: " + simpDate.format(date));
            output.println("\n");
            output.println("         Cross-Reference Listing Description");
            output.println("Labels: The label name that appears in the source program.");
            output.println("Mem_Loc: Memory location of a label in the memory. If the value");
            output.println("         starts with 'R', it represents a register.");
            output.println("Def_Line: Defined line number of a label in the source code.");
            output.println("Ref_line: Referenced line number(s) of a label in the source code.");
            output.println();
            output.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            output.println("Labels         " + "  Mem_Loc" + "     Def_Line" + "     Ref_line");
//            for (int i = 0; i < codeList.size(); i++){
//                if (labels[i] != null){
//                    String memoryAddress = intToHex(Integer.toString(labelMap.get(labels[i]))); 
//                    output.printf("%-15s%7s%12d%8s%-15s", labels[i], memoryAddress, i+1, " ", referenceLine.get(labels[i]));
//                    output.println();
//                }
//            }//end cross reference listing
//            
            output.close();    //Close the file
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
