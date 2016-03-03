/**
 * Program: Clock.java
 * 
 * Purpose:
 * 
 * @author:
 * 
 * date/ver:
 */
package machine.presenter;

import java.util.Timer;
import java.util.TimerTask;


public class Clock {

	private final MachineController controller;
	Timer timer = new Timer();
        private TimerTask task;
	private int instructionPointer;
	private final Disassembler disassembler = new Disassembler();
        private int speed;
	
	/**
	 * Creates a Clock object every time an instruction is executed
	 * @param controller
	 */
	public Clock(MachineController controller) {
		this.controller = controller;
	}
        
        public void setSpeed(int speed) {
            this.speed = speed;
        }
        
        public void run() {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    step();
                }
            };
            timer.scheduleAtFixedRate(task, 0, speed);
        }

	/**
	 * Calls the fetch method and cancels the timer
	 * once the Instruction Register is greater than 0xFF
	 */
	public synchronized void step() {
		if (controller.getInstructionPointer() > 0xFF) {
			timer.cancel();
		}
		fetch();
	}
	
	/**
	 * Gets the instruction to be executed and calls the decode method
	 */
	private void fetch() {
		int[] instructions = controller.getInstructionRegister();
		decode(instructions);
	}

	/**
	 * Performs the instructions that were fetched.
	 * @param instructions
	 */
	private void decode(int[] instructions) {
		// get the opcode
		String operation = 
			Integer.toHexString(instructions[0]);
		String operand = Integer.toHexString(instructions[1]);
		// if operation a single digit concatenate 0 to beginning 
		if(operation.length() == 1)
			operation = "0" + operation;
		if(operand.length() == 1)
			operand = "0" + operand;
		// first nibble of opcode
		char firstOpcode = Character.toUpperCase(operation.charAt(0));
		// second nibble of opcode
		char secondOpcode = Character.toUpperCase(operation.charAt(1));
		char firstOperand = Character.toUpperCase(operand.charAt(0));
		char secondOperand = Character.toUpperCase(operand.charAt(1));
		int secondNibble = Character.digit(secondOpcode,16);
		int thirdNibble = Character.digit(firstOperand,16);
		int fourthNibble = Character.digit(secondOperand,16);
		int secondByte = Integer.parseInt(operand, 16);
		switch(firstOpcode){
			case '1':
				directLoad(secondNibble,secondByte);
				execute();
				break;
			case '2': 
				//immediateLoad(secondNibble,secondByte);
                                //jl948836 - 2/20/16 - modified immediate load to take
                                //third parameter. thirdNibble
                                //thirdNibble will be a flag incases of the immediateLoad
                                //being compiled by the rload instruction.
				immediateLoad(secondNibble, secondByte, thirdNibble);
                                execute();
				break;
			case '3': 
				directStore(secondNibble, secondByte);
				execute();
				break;
			case '4': 
				move(thirdNibble,fourthNibble);
				execute();
				break;
			case '5': 
				add(secondNibble,thirdNibble,fourthNibble);
				execute();
				break;
			case '6': 
				if (secondOpcode == '0') { // regular call
					call();
					execute(secondByte);
				}else if (secondOpcode == '1') { // regular return
					ret(secondByte);
					execute(controller.getInstructionPointer());
				}else if (secondOpcode == '2') { // special call
					scall();
					execute(secondByte);
				}else if (secondOpcode == '3') { // special return
					sret(secondByte);
					execute(controller.getInstructionPointer());
				}else if(secondOpcode == '4'){ // push
					push(thirdNibble);
					execute();
				}else if (secondOpcode == '5'){ // pop 
					pop(thirdNibble);
					execute();
				}
				break;
			case '7': 
				or(secondNibble,thirdNibble,fourthNibble);
				execute();
				break;
			case '8': 
				and(secondNibble,thirdNibble,fourthNibble);
				execute();
				break;
			case '9': 
				xor(secondNibble,thirdNibble,fourthNibble);
				execute();
				break;
			case 'A':
				ror(secondNibble,secondByte);
				execute();
				break;
			case 'B': // jmpeq
				if (jump(secondNibble)) {
					execute(secondByte);
				} else {
					execute();
				}
				break;
			// halt operation
			case 'C': halt();
				break;
			case 'D': 
				if (secondOpcode == '0') { //ILOAD
					iload(thirdNibble, fourthNibble);
				} else if (secondOpcode == '1') { //ISTORE
					istore(thirdNibble, fourthNibble);
				} else if (secondOpcode == '2') { //RLOAD
					rload(thirdNibble, fourthNibble);
				}
				execute();
				break;
			case 'E':
				rstore(secondNibble, thirdNibble, fourthNibble);
				execute();
				break;
			case 'F':
				if(jmplt(secondNibble)){         // change "jmple" to "jmplt"
					execute(secondByte);
				} else {
					execute();
				}
				break;
			default: execute();
				break;
		}
		
		updateDisassembleDisplay();
		
	}

	/**
	 * Regular execute
	 */
	private void execute() {
		instructionPointer = controller.getInstructionPointer();
		instructionPointer += 2;
		controller.setInstructionPointer(instructionPointer);
		controller.setInstructionRegister();
		controller.refreshMachineView();
	}

	/**
	 * Overloaded execute for jump instructions
	 * @param location
	 */
	private void execute(int location) {
		instructionPointer = controller.getInstructionPointer();
		instructionPointer = location;
		controller.setInstructionPointer(instructionPointer);
		controller.setInstructionRegister();
		controller.refreshMachineView();
	}
	
	/**
	 * Opcode 1 - LOAD
	 * @param register
	 * @param memIndex
	 */
	private void directLoad(int register,int memIndex) {
		String data = controller.getMemoryValue(memIndex);
		controller.setRegisterValue(register, data);
		if (register == 0x0F){
			printRegisterF();
		}
	}
	
	/**
         *  jl948836 - 2/20/16 - modified immediate load to take
         *  third parameter. thirdNibble
         * Opcode 2 - LOAD
	 * @param register
	 * @param memIndex
	 */
	private void immediateLoad(int register, int memIndex, int rloadFlag) {
		String memory = Integer.toHexString(memIndex);
		controller.setRegisterValue(register, memory);
                if (register == 0x0F && rloadFlag != 15){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode 3 - STORE
	 * @param register
	 * @param memIndex
	 */
	private void directStore(int register, int memIndex) {
		String value = controller.getRegisterValue(register);
		controller.setMemoryValue(memIndex, value);
	}
	
	/**
	 * Opcode 4 - MOVE
	 * @param fromRegister
	 * @param toRegister
	 */
	private void move(int fromRegister, int toRegister) {
		String data = controller.getRegisterValue(fromRegister);
		controller.setRegisterValue(toRegister, data);
		if (toRegister == 0x0F){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode 5 - ADD
	 * @param resultRegister
	 * @param oneRegister
	 * @param twoRegister
	 */
	private void add(int resultRegister, int oneRegister, int twoRegister) {
		int x = Integer.parseInt(controller.getRegisterValue(oneRegister),16);
		int y = Integer.parseInt(controller.getRegisterValue(twoRegister),16);
		int result = (x + y) & 0xFF;
		String value = Integer.toHexString(result);
		controller.setRegisterValue(resultRegister, value);
		if (resultRegister == 0x0F){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode 60 - CALL
	 */
	private void call() {
		int ip = controller.getInstructionPointer();
		int sp = Integer.parseInt(controller.getRegisterValue(0xE),16)-1;
		int bp = Integer.parseInt(controller.getRegisterValue(0xD), 16);
		String stackPointer = Integer.toHexString(sp);
		controller.setRegisterValue(0xE, stackPointer);
		controller.setMemoryValue(sp, Integer.toHexString(ip+2));
		controller.createActivationRecord(ip,bp);
	}
	
	/**
	 * Opcode 61 - RETURN
	 */
	private void ret(int spAdd) {
		int sp = Integer.parseInt(controller.getRegisterValue(0xE),16);
		int value = Integer.parseInt(controller.getMemoryValue(sp),16);
		controller.setInstructionPointer(value);
		sp += 1 + spAdd;
		String stackPointer = Integer.toHexString(sp);
		controller.setRegisterValue(0xE, stackPointer);
		controller.deleteActivationRecord();
	}
	/**
	 * Opcode 62 - SCALL
	 */
	private void scall() {
		push(0x0C);
		call();
		push(0x0D);
		move(0x0E,0x0D);
	}

	/**
	 * Opcode 63 - SRETURN
	 */
	private void sret(int spAdd) {
		move(0x0D,0x0E);
		ret(spAdd);
	}
	
	/**
	 * Opcode 64 - PUSH
	 * @param register
	 */
	private void push(int register) {
		String value = controller.getRegisterValue(register);
		int sp = Integer.parseInt(controller.getRegisterValue(0xE),16) - 1;
		String stackPointer = Integer.toHexString(sp);
		controller.setRegisterValue(0xE, stackPointer);
		controller.setMemoryValue(sp, value);
	}

	/**
	 * Opcode 65 - POP
	 * @param register
	 */
	private void pop(int register) {
		int sp = Integer.parseInt(controller.getRegisterValue(0xE),16);
		String value = controller.getMemoryValue(sp);
		controller.setRegisterValue(register, value);
		sp += 1;
		String stackPointer = Integer.toHexString(sp);
		controller.setRegisterValue(0xE, stackPointer);
		if (register == 0x0F){
			printRegisterF();
		}
	}
	/**
	 * Opcode 7 - OR
	 * @param resultRegister
	 * @param oneRegister
	 * @param twoRegister
	 */
	private void or(int resultRegister, int oneRegister, int twoRegister) {
		int x = Integer.parseInt(controller.getRegisterValue(oneRegister),16);
		int y = Integer.parseInt(controller.getRegisterValue(twoRegister),16);
		int result = x | y;
		String value = Integer.toHexString(result);
		controller.setRegisterValue(resultRegister, value);
		if (resultRegister == 0x0F){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode 8 - AND
	 * @param resultRegister
	 * @param oneRegister
	 * @param twoRegister
	 */
	private void and(int resultRegister, int oneRegister, int twoRegister) {
		int x = Integer.parseInt(controller.getRegisterValue(oneRegister),16);
		int y = Integer.parseInt(controller.getRegisterValue(twoRegister),16);
		int result = x & y;
		String value = Integer.toHexString(result);
		controller.setRegisterValue(resultRegister, value);	
		if (resultRegister == 0x0F){
			printRegisterF();
		}
	}

	/**
	 * Opcode 9 - XOR
	 * @param resultRegister
	 * @param oneRegister
	 * @param twoRegister
	 */
	private void xor(int resultRegister, int oneRegister, int twoRegister) {
		int x = Integer.parseInt(controller.getRegisterValue(oneRegister),16);
		int y = Integer.parseInt(controller.getRegisterValue(twoRegister),16);
		int result = x ^ y;
		String value = Integer.toHexString(result);
		controller.setRegisterValue(resultRegister, value);	
		if (resultRegister == 0x0F){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode A - ROR
	 * @param register
	 * @param times
	 */
	private void ror(int register, int times) {
		int bitPattern = Integer.parseInt(controller.getRegisterValue(register), 16);
		int carry;
		int shifted;
		for (int i = 0; i < times; i++){
			carry = bitPattern << 7;
			shifted = bitPattern >> 1;
			bitPattern = (carry | shifted) & 0xFF;
		}
		controller.setRegisterValue(register, Integer.toHexString(bitPattern));
		if (register == 0x0F){
			printRegisterF();
		}
	}
	
	/**
	 * Opcode B - JMPEQ and JMP
	 * @param register
	 * @return
	 */
	private boolean jump(int register) {
                return (controller.getRegisterValue(register).equals(controller.getRegisterValue(0)));
	}
	
	
	/**
	 * Opcode C - HALT
	 */
	private void halt() {
		timer.cancel();
	}
	
	/**
	 * Opcode D0 - ILOAD
	 * @param register
	 * @param pointer
	 */
	private void iload(int register, int pointer) {
		String address = controller.getRegisterValue(pointer);
		String value = controller.getMemoryValue(Integer.parseInt(address,16));
		controller.setRegisterValue(register, value);
		if (register == 0x0F){
			printRegisterF();
		}
	}
	/**
	 * Opcode D1 - ISTORE
	 * @param register
	 * @param pointer
	 */
	private void istore(int register, int pointer) {
		String value = controller.getRegisterValue(register);
		int address = Integer.parseInt(controller.getRegisterValue(pointer),16);
		controller.setMemoryValue(address, value);
	}
	
	/**
	 * Opcode D2 - RLOAD
	 * @param register
	 * @param pointer
	 */
	private void rload(int register, int pointer) {
		String offset = controller.getRegisterValue(register);
		int realOffset = Character.digit(offset.charAt(1), 16);
		if (realOffset > 7) {
			realOffset -= 16;
		}
		int address = Integer.parseInt(controller.getRegisterValue(pointer), 16);
		int offsetAddress = address + realOffset;
		String data = controller.getMemoryValue(offsetAddress);
		controller.setRegisterValue(register, data);
		if (register == 0x0F){
			printRegisterF();
		}
	}
	/**
	 * Opcode E - RSTORE
	 * @param offset
	 * @param register
	 * @param pointer
	 */
	private void rstore(int offset, int register, int pointer) {
		if(offset > 7) {
			offset -= 16;
		}
		String value = controller.getRegisterValue(register);
		int address = Integer.parseInt(controller.getRegisterValue(pointer),16);
		int offsetAddress = address + offset;
		controller.setMemoryValue(offsetAddress, value);
	}
	/**
	 * Opcode F - JMPLT
	 * @param register
	 * @return
	 */
	private boolean jmplt(int register) {        // change "jmple" to "jmplt"
		int value = Integer.parseInt(controller.getRegisterValue(register),16);
		int registerZero = Integer.parseInt(controller.getRegisterValue(0),16);
//		if (registerZero > 127) {
//			registerZero -= 256;
//		}
//		if (value > 127) {
//			value -= 256;
//		}
		return (value < registerZero); //change "<=" to "<"
	}
	
	/**
	 * Prints out register F.  
	 */
	private void printRegisterF() {
		char c = ' ';
		int temp = Integer.parseInt(controller.getRegisterValue(15), 16);
		if ((temp >= 32 && temp < 127) || temp == '\n') {
			c = (char)temp;
		}
		controller.setConsoleText(c);
	}
	
	/**
	 * Updates the disassemble console text after each execute
	 */
	private void updateDisassembleDisplay() {

		boolean update = true;
		// set disassembler console text
		// if IP is odd for whatever reason, we aren't updating the
		// disassembler console
		int IP = controller.getInstructionPointer();
		int relativeIP = 6;
		String[] codes = {"","","","","","","","","","","","","",""};
		if ( (IP > 5 && IP < 249) && ( (IP % 2) == 0)) { // this should test first
			codes[0] = controller.getMemoryValue(IP - 6);
			codes[1] = controller.getMemoryValue(IP - 5);
			codes[2] = controller.getMemoryValue(IP - 4);
			codes[3] = controller.getMemoryValue(IP - 3);
			codes[4] = controller.getMemoryValue(IP - 2);
			codes[5] = controller.getMemoryValue(IP - 1);
			codes[6] = controller.getMemoryValue(IP); // << IP here
			codes[7] = controller.getMemoryValue(IP + 1);
			codes[8] = controller.getMemoryValue(IP + 2);
			codes[9] = controller.getMemoryValue(IP + 3);
			codes[10] = controller.getMemoryValue(IP + 4);
			codes[11] = controller.getMemoryValue(IP + 5);
			codes[12] = controller.getMemoryValue(IP + 6);
			codes[13] = controller.getMemoryValue(IP + 7);
			relativeIP = 3;
		} else if (IP < 5) { // got to handle the edge cases...
			codes[0] = controller.getMemoryValue(0);
			codes[1] = controller.getMemoryValue(1);
			codes[2] = controller.getMemoryValue(2);
			codes[3] = controller.getMemoryValue(3);
			codes[4] = controller.getMemoryValue(4);
			codes[5] = controller.getMemoryValue(5);
			codes[6] = controller.getMemoryValue(6);
			codes[7] = controller.getMemoryValue(7);
			codes[8] = controller.getMemoryValue(8);
			codes[9] = controller.getMemoryValue(9);
			codes[10] = controller.getMemoryValue(10);
			codes[11] = controller.getMemoryValue(11);
			codes[12] = controller.getMemoryValue(12);
			codes[13] = controller.getMemoryValue(13);
			relativeIP = IP / 2;
		} else if (IP > 249) {
			codes[0] = controller.getMemoryValue(242);
			codes[1] = controller.getMemoryValue(243);
			codes[2] = controller.getMemoryValue(244);
			codes[3] = controller.getMemoryValue(245);
			codes[4] = controller.getMemoryValue(246);
			codes[5] = controller.getMemoryValue(247);
			codes[6] = controller.getMemoryValue(248);
			codes[7] = controller.getMemoryValue(249);
			codes[8] = controller.getMemoryValue(250);
			codes[9] = controller.getMemoryValue(251);
			codes[10] = controller.getMemoryValue(252);
			codes[11] = controller.getMemoryValue(253);
			codes[12] = controller.getMemoryValue(254);
			codes[13] = controller.getMemoryValue(255);
			if (IP == 250) { 
				relativeIP = 4;
			} else if (IP == 252) {
				relativeIP = 5;
			} else {
				relativeIP = 6;
			}
		} else { // IP is odd... 
			update = false;
		}
		
		if (update) {
			// send codes off to disassembler
			String consoleText;
			if (codes[0].equals("D2")) { // handle rload specially
				String loadByte1 = "";
				String loadByte2 = "";
				if ( (IP > 5) && (IP < 249) ) { 
					loadByte1 = controller.getMemoryValue(IP - 8);
					loadByte2 = controller.getMemoryValue(IP - 7);
					relativeIP++;
				} else if (IP > 249) {
					loadByte1 = controller.getMemoryValue(240);
					loadByte2 = controller.getMemoryValue(241);
					relativeIP++;
				}
                                
				String[] fixedCodes = new String[16];
				for (int i = 0; i < 14; i++) {
					fixedCodes[i+2] = codes[i];
				}
				fixedCodes[0] = loadByte1;
				fixedCodes[1] = loadByte2;
				consoleText = disassembler.getConsoleDisassemble(fixedCodes, relativeIP);
			} else {
				consoleText = disassembler.getConsoleDisassemble(codes, relativeIP);
			}
			controller.setDisassText(consoleText);
		}	
	}
	
}