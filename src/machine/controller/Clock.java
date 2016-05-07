/**
 * Program: Clock.java
 * 
 * Purpose:
 * 
 * @author:
 * 
 * date/ver:
 */

/**
 * Change Log
 *
 * # author - date: description
 * 1 jl948836 - 02/20/16: Added a third parameter to immediateLoad; Feeds in
 *                        third Nibble of instruction; If it is hex F; then
 *                        the immediateLoad is part of the rload instruction,
 *                        and registerF shouldn't print a value
 * 
 * 3 jl948836 - 03/24/16: The additional 1 is now generated at byte code, instead
 *                        of by the clock
 * 
 * 4 jl948836 - 04/01/16: Fixed the SCALL and SRET functions.
 * 
 * 5 jl948836 - 04/01/16: Corrected format of ByteCode for iload and move
 * 
 * 6 jl948836 - 04/01/16:
 * 4 jl948836 - 04/01/16: Fixed the SCALL and SRET functions
 * 
 * 5 mv935583 - 04/11/16: Implemented changed to add shift instructions.
 * 
 * 6 jl948836 - 04/19/16: flipped RSTORE, register operands were backward.
 * 
 * 7 mv935583 - 04/25/16: Added code for tracking number of instructions executed
 */

/* Change Log
 * Guojun Liu  03/08/16 
 * 1. Modified the fetch phase
 * 2. Modified the HALT condition in decode phase
 * 3. Modified two execute phase
*/

package machine.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Clock {

    private final MachineController controller;
    Timer timer = new Timer();
    private TimerTask task;
    private int instructionPointer;
    private final Disassembler disassembler = new Disassembler();
    private int speed;
    private int instructionCount;   //CHANGELOG: 7
    
    private static final Map<String, String> VALIDOPERATIONMAP; //CHANGE LOG BEING: 36
    static {
        VALIDOPERATIONMAP = new HashMap<>();
        VALIDOPERATIONMAP.put("00", "00"); //Matters
        VALIDOPERATIONMAP.put("1", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("2", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("3", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("4", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("5", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("60", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("61", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("62", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("63", "01"); //Matters
        VALIDOPERATIONMAP.put("64", "[0-9a-fA-F]{1}0"); //Matters
        VALIDOPERATIONMAP.put("65", "[0-9a-fA-F]{1}0"); //Matters
        VALIDOPERATIONMAP.put("7", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("8", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("9", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("A0", "[0-9a-fA-F]{1}[0-8]{1}"); //Matters
        VALIDOPERATIONMAP.put("A1", "[0-9a-fA-F]{1}[0-8]{1}"); //Matters
        VALIDOPERATIONMAP.put("A2", "[0-9a-fA-F]{1}[0-8]{1}"); //Matters
        VALIDOPERATIONMAP.put("A3", "[0-9a-fA-F]{1}[0-8]{1}"); //Matters
        VALIDOPERATIONMAP.put("A4", "[0-9a-fA-F]{1}[0-8]{1}"); //Matters
        VALIDOPERATIONMAP.put("B", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("B0", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("C0", "00"); //Matters
        VALIDOPERATIONMAP.put("D0", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("D1", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("D2", "[0-9a-fA-F]{2}");
        VALIDOPERATIONMAP.put("E", "[0-9a-fA-F]{3}");
        VALIDOPERATIONMAP.put("F", "[0-9a-fA-F]{3}");
    }
    
    /**
     * Creates a Clock object every time an instruction is executed.
     * 
     * @param controller - a machine controller of which to set to this clock.
     */
    public Clock(MachineController controller) {
        this.controller = controller;
    }

    /**
     * Sets the clock speed based on the passed in variable.
     * 
     * @param speed - the speed at which the clock will be set.
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Begins running the machine causing it to step continuously based on the 
     * set speed of the machine.
     */
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
     * once the Instruction Register is greater than 0xFF.
     */
    public synchronized void step() {
        if (controller.getInstructionPointer() > 0xFF) {
            timer.cancel();
        }
        fetch();
        controller.setInstructionCounterText(instructionCount++);   //CHANGELOG: 7
    }

    /**
     * Gets the instruction to be executed and calls the decode method.
     */
    private void fetch() {
        String[] instructions = controller.getInstructionFromIP();
        decode(instructions);
    }

    /**
     * Decodes the Operation and its Operands, checking for valid instructions
     * and then calls the Execute method.
     * 
     * @param instructions - [Operation, Operand]
     */
    private void decode(String[] instructions) {
        String operation = instructions[0];
        String operand = instructions[1];
        String opNibble = operation.substring(0, 1);
        boolean validOperation = false;
        //Single Nibble Operations
        if (VALIDOPERATIONMAP.containsKey(opNibble)) {
            //System.out.println((operation.substring(1,2) + operand).matches(VALIDOPERATIONMAP.get(opNibble)) + " " + operand + " " + VALIDOPERATIONMAP.get(opNibble));
            if ((operation.substring(1, 2) + operand).matches(VALIDOPERATIONMAP.get(opNibble))) {
                validOperation = true;
            }//end if
        }//end if
        //Byte Operations
        else if (VALIDOPERATIONMAP.containsKey(operation)) {           
            //System.out.println(operand.matches(VALIDOPERATIONMAP.get(operation)) + " " + operand + " " + VALIDOPERATIONMAP.get(operation));
            if (operand.matches(VALIDOPERATIONMAP.get(operation))) {
                validOperation = true;
            }//end if
        }//end else if
        else {
            int ip = controller.getInstructionPointer();
            //if not at address 0 or 1
            if (ip > 1) {
                ip -= 2; //current executing instruction
            }
            //System.out.println(ip);
            controller.getFatalRunTimeErrorList().add("Invalid Instruction Decoded at "
                    + "memory address " + Integer.parseInt(Integer.toString(ip), 16));
            //Do not execute
        }//end else
        
        if (validOperation) {
            //Arguments to pass
            int secondNibble = Integer.parseInt(operation.substring(1, 2), 16);
            int thirdNibble = Integer.parseInt(operand.substring(0, 1), 16);
            int fourthNibble = Integer.parseInt(operand.substring(1, 2), 16);
            int secondByte = Integer.parseInt(operand, 16);
            execute(secondByte, opNibble, secondNibble, thirdNibble, fourthNibble);
        }
    }
    
    /**
     * 
     * @param firstNibble - always part of the Operation
     * @param secondNibble - sometimes part of the operand, sometimes part of the Operation
     * @param thirdNibble - operand
     * @param fourthNibble - operand
     */
    private void execute(int secondByte, String firstNibble, int secondNibble, int thirdNibble, int fourthNibble) {
        boolean specialIP = false;
        int location = 0;
        switch(firstNibble) {
            case "1":
                directLoad(secondNibble,secondByte);
                break;
            case "2":
                //CHANGE LOG BEGIN - 1
                immediateLoad(secondNibble, secondByte);
                //CHANGE LOG END - 1
                break;
            case "3": 
                directStore(secondNibble, secondByte);
                break;
            case "4": //CHANGE LOG: 6
                rload(secondNibble, thirdNibble, fourthNibble);
                break;
            case "5": 
                add(secondNibble,thirdNibble,fourthNibble);
                break;
            case "6": 
                switch(secondNibble) {
                    case 0:
                        // regular call
                        call();
                        specialIP = true;
                        location = secondByte;
                        break;
                    case 1:
                        // regular return
                        ret(secondByte);
                        specialIP = true;
                        location = controller.getInstructionPointer();
                        break;
                    case 2:
                        scall();
                        location = secondByte;
                        specialIP = true;
                        break;
                    case 3:
                        sret(secondByte);
                        location = controller.getInstructionPointer();
                        specialIP = true;
                        break;
                    case 4:
                        // push
                        push(thirdNibble);
                        break;
                    case 5:
                        // pop
                        pop(thirdNibble);
                        break;
                    default:
                        break;
                }
                break;
            case "7": 
                or(secondNibble,thirdNibble,fourthNibble);
                break;
            case "8": 
                and(secondNibble,thirdNibble,fourthNibble);
                break;
            case "9": 
                xor(secondNibble,thirdNibble,fourthNibble);
                break;
            case "A":
                switch (secondNibble) { //BEGIN CHANGE LOG: 5
                    case 0:
                        ror(thirdNibble, fourthNibble);
                        break;
                    case 1:
                        rol(thirdNibble, fourthNibble);
                        break;
                    case 2:
                        sra(thirdNibble, fourthNibble);
                        break;
                    case 3:
                        srl(thirdNibble, fourthNibble);
                        break;
                    case 4:
                        sl(thirdNibble, fourthNibble);
                        break;
                    default:
                        break;
                }
                break;  //END CHANGE LOG: 5
            case "B":
                if (jump(secondNibble)) {
                    location = secondByte;
                    specialIP = true;
                } else {
                }
                break;
            // halt operation
            case "C": halt();
                controller.updateIPwhenHalt();
                break;
            case "D": 
                if (secondNibble == 0) { //ILOAD
                    iload(thirdNibble, fourthNibble);
                } 
                else if (secondNibble == 1) { //ISTORE
                    istore(thirdNibble, fourthNibble);
                } 
                else if (secondNibble == 2) { //MOVE
                    move(thirdNibble,fourthNibble); //CHANGE LOG: 5
                }
                break;
            case "E":
                rstore(secondNibble, thirdNibble, fourthNibble);
                break;
            case "F":
                if(jmplt(secondNibble)){ // change "jmple" to "jmplt"
                    location = secondByte;
                    specialIP = true;
                }
                break;
            default: 
                break;
        }
        if (specialIP){
            updateInstructionPointer(location);
        }
        else {
            updateInstructionPointer();
        }
        updateDisassembleDisplay();
    }
    
    private void updateInstructionPointer() {
        instructionPointer = controller.getInstructionPointer();
        instructionPointer += 2;
        controller.setInstructionPointer(instructionPointer);
        controller.setIPinstruction();    //update MAR
        controller.setInstructionRegister();
        controller.refreshMachineView();
    }
    
    private void updateInstructionPointer(int location){
        instructionPointer = location;
        controller.setInstructionPointer(instructionPointer);
        controller.setInstructionRegisterForJump();
        controller.setIPinstruction();    //update MAR
        controller.refreshMachineView();
    }
    
    /**
     * Returns the number of instructions ran by the clock.
     * 
     * @return The current instruction count that the clock has executed.
     */
    public int getInstructionCount(){
        return this.instructionCount;
    }
    
    /**
     * Resets the clock internal instruction count for the purpose of machine
     * assemble and reset.
     */
    public void resetInstructionCount(){
        this.instructionCount = 0;
    }   //CHANGELOG END: 7

    /**
     * Opcode 1 - LOAD
     * 
     * Loads indicated register with value from indicated memory index.
     * 
     * @param register - register to load.
     * @param memIndex - memory index of value to load to register.
     */
    private void directLoad(int register,int memIndex) {
        String data = controller.getMemoryValue(memIndex);
        controller.setRegisterValue(register, data);
        if (register == 0x0F){
            printRegisterF();
        }
    }

    /**
     * Opcode 2 - LOAD
     * 
     * Loads indicated register with value from indicated memory index. The
     * memory index can be a literal or an address.
     * 
     * @param register - register to load.
     * @param memIndex - memory index of value to load to register. Can be 
     * literal or an address.
     */
    //CHANGE LOG BEGIN - 1
    private void immediateLoad(int register, int memIndex) {
        String memory = Integer.toHexString(memIndex);
        controller.setRegisterValue(register, memory);
        if (register == 0x0F){
            printRegisterF();
        }
    }
    //CHANGE LOG END - 1
    
    /**
     * Opcode 3 - STORE
     * 
     * Stores the value in the indicated register into the indicated memory index.
     * 
     * @param register - register that contains the value to store into memory.
     * @param memIndex - memory index to store value contained in register.
     */
    private void directStore(int register, int memIndex) {
        String value = controller.getRegisterValue(register);
        controller.setMemoryValue(memIndex, value);
    }

    /**
     * Opcode 4 - RLOAD
     * 
     * Loads the indicated register with the value from memory pointed at by the 
     * pointer plus the offset
     * 
     * @param register - register to load.
     * @param pointer - register that holds the memory index of value to load to
     * register.
     * @param offset - value added to pointer to offset memory index of value to 
     * load to register.
     */
    private void rload(int offset, int register, int pointer) {
        if (offset > 7) {
            offset -= 16;
        }
        int address = Integer.parseInt(controller.getRegisterValue(pointer), 16);
        int offsetAddress = address + offset;
        String data = controller.getMemoryValue(offsetAddress);
        controller.setRegisterValue(register, data);
        if (register == 0x0F){
            printRegisterF();
        }
    }

    /**
     * Opcode 5 - ADD
     * 
     * Takes the values in oneRegister and twoRegister, adds them and masks with
     * 0xFF to ensure one byte size and stores result in resultRegister.
     * 
     * @param resultRegister - holds the addition result.
     * @param oneRegister - contains value to be added.
     * @param twoRegister - contains value to be added.
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
     * 
     * Sets the instruction pointer to address specified, decrements the stack pointer and 
     * pushed the return address onto the stack.
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
     * 
     * Sets the instruction pointer with address on top of the stack, increments the 
     * stack pointer by value of spAdd.
     * 
     * @param spAdd - value of which to increment stack pointer.
     */
    private void ret(int spAdd) {
        int sp = Integer.parseInt(controller.getRegisterValue(0xE),16);
        int value = Integer.parseInt(controller.getMemoryValue(sp),16);
        controller.setInstructionPointer(value);
        sp += spAdd; //CHANGE LOG: 3
        String stackPointer = Integer.toHexString(sp);
        controller.setRegisterValue(0xE, stackPointer);
        controller.deleteActivationRecord();
    }
    /**
     * Opcode 62 - SCALL
     * 
     * Runs call, pushes the base pointer onto the stack and moves the base pointer
     * into the stack pointer.
     * 
     */
    //CHANGE LOG BEGIN: 4
    private void scall() {
        call();
        push(0x0D);
        move(0x0D,0x0E);
    }
    //CHANGE LOG END: 4

    /**
     * Opcode 63 - SRETURN
     * 
     * Pops the top of the stack into the base pointer, runs ret with spAdd argument,
     * moves stack pointer into base pointer.
     * 
     * @param spAdd - value of which to increment stack pointer.
     */
    //CHANGE LOG BEGIN: 4
    private void sret(int spAdd) {
        pop(0x0D); //Reset Frame of Reference
        ret(spAdd); //Return
        move(0x0E,0x0D); //Clean arguments off the Stack
    }
    //CHANGE LOG END: 4

    /**
     * Opcode 64 - PUSH
     * 
     * Decrements the stack pointer then stores value contained in register argument onto the stack.
     * 
     * @param register - contains value to be pushed onto the stack.
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
     * 
     * Loads from the value that the stack pointer is pointing at into specified 
     * target register and increments the stack pointer.
     * 
     * @param register - target to load value from stack.
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
     * 
     * Takes the values in oneRegister and twoRegister, performs OR bit 
     * operation them and stores result in resultRegister.
     * 
     * @param resultRegister - holds the OR result.
     * @param oneRegister - contains value to be OR.
     * @param twoRegister - contains value to be OR.
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
     * 
     * Takes the values in oneRegister and twoRegister, performs AND bit 
     * operation them and stores result in resultRegister.
     * 
     * @param resultRegister - holds the AND result.
     * @param oneRegister - contains value to be AND.
     * @param twoRegister - contains value to be AND.
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
     * 
     * Takes the values in oneRegister and twoRegister, performs XOR bit 
     * operation them and stores result in resultRegister.
     * 
     * @param resultRegister - holds the XOR result.
     * @param oneRegister - contains value to be XOR.
     * @param twoRegister - contains value to be XOR.
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
     * 
     * Gets the value from register argument and right shifts it times number of
     * times and takes the MSB and pads to LSB.
     * 
     * @param register - target register to be rotated.
     * @param times - number of times to rotate target register. 
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
     * Opcode A - ROL
     * 
     * Gets the value from register argument and left shifts it times number of
     * times and takes the MSB and pads to LSB.
     * 
     * @param register - target register to be rotated.
     * @param times - number of times to rotate target register. 
     */
    private void rol(int register, int times){  //BEGIN CHANGE LOG: 5
        int bitPattern = Integer.parseInt(controller.getRegisterValue(register), 16);
        int carry;
        int shifted;
        for (int i = 0; i < times; i++){
            carry = bitPattern >> 7;
            shifted = bitPattern << 1;
            bitPattern = (carry | shifted) & 0xFF;
        }
        controller.setRegisterValue(register, Integer.toHexString(bitPattern));
        if (register == 0x0F){
            printRegisterF();
        }
    }
    
    /**
     * Opcode A - SRA
     * 
     * Gets the value from register argument and does a right arithmetic shift times
     * times.
     * 
     * @param register - target register to be shifted.
     * @param times - number of times to shift the target register.
     */
    private void sra(int register, int times){
        int bitPattern = Integer.parseInt(controller.getRegisterValue(register), 16);
        for (int i = 0; i < times; i++){
            int leadingBit = bitPattern & 0x80;
            bitPattern = bitPattern >> 1;
            bitPattern = (leadingBit | bitPattern) & 0xFF;
        }
        controller.setRegisterValue(register, Integer.toHexString(bitPattern));
        if (register == 0x0F){
            printRegisterF();
        }
    }
    
    /**
     * Opcode A - SRL
     * 
     * Gets the value from register argument and does a right logical shift times
     * times.
     * 
     * @param register - target register to be shifted.
     * @param times - number of times to shift the target register.
     */
    private void srl(int register, int times){
        int bitPattern = Integer.parseInt(controller.getRegisterValue(register), 16);
        for (int i = 0; i < times; i++){
            bitPattern = bitPattern >>> 1;
            bitPattern = bitPattern & 0xFF;
        }
        controller.setRegisterValue(register, Integer.toHexString(bitPattern));
        if (register == 0x0F){
            printRegisterF();
        }
    }
    
    /**
     * Opcode A - SRA
     * 
     * Gets the value from register argument and does a right arithmetic shift times
     * times.
     * 
     * @param register - target register to be shifted.
     * @param times - number of times to shift the target register.
     */
    private void sl(int register, int times){
        int bitPattern = Integer.parseInt(controller.getRegisterValue(register), 16);
        for (int i = 0; i < times; i++){
            bitPattern = bitPattern << 1;
            bitPattern = bitPattern & 0xFF;
        }
        controller.setRegisterValue(register, Integer.toHexString(bitPattern));
        if (register == 0x0F){
            printRegisterF();
        }
    }   //END CHANGE LOG: 5

    /**
     * Opcode B - JMPEQ and JMP
     * 
     * 
     * @param register
     * @return
     */
    private boolean jump(int register) {
        return (controller.getRegisterValue(register).equals(controller.getRegisterValue(0)));
    }

    /**
     * Opcode C - HALT
     * 
     * Halts execution and stops the timer.
     * 
     */
    private void halt() {
        timer.cancel();
    }

    /**
     * Opcode D0 - ILOAD
     * 
     * Loads the value pointed out by pointer to the target register.
     * 
     * @param register - target register to be loaded.
     * @param pointer - memory index whose contents will be loaded into the target 
     * register 
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
     * 
     * Stores the value contained in the target register into the location pointed to
     * by the pointer register.
     * @param register - holds the value to be stored into memory.
     * @param pointer - holds the location in memory to store into.
     */
    //CHANGE LOG: 5
    private void istore(int pointer, int register) {
        String value = controller.getRegisterValue(register);
        int address = Integer.parseInt(controller.getRegisterValue(pointer),16);
        controller.setMemoryValue(address, value);
    }

    /**
     * Opcode D2 - MOVE
     * 
     * A register to register moves, takes the value stored in the fromRegister 
     * and moves it into the toRegister.
     * @param fromRegister - holds the value to be loaded.
     * @param toRegister - register to be loaded into.
     */
    //CHANGE LOG: 5
    private void move(int toRegister, int fromRegister) {
        String data = controller.getRegisterValue(fromRegister);
        controller.setRegisterValue(toRegister, data);
        if (toRegister == 0x0F){
            printRegisterF();
        }
    }
    
    /**
     * Opcode E - RSTORE
     * 
     * Stores the value in the register argument into the location specified by the 
     * pointer plus the value of the offset.
     * 
     * @param offset - value to be added to the pointer.
     * @param register - holds the value to be stored in memory.
     * @param pointer - location in memory to store into.
     */
    private void rstore(int offset, int pointer, int register) { //CHANGE LOG: 6
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
     * 
     * 
     * @param register
     * @return
     */
    private boolean jmplt(int register) { // change "jmple" to "jmplt"
        int value = Integer.parseInt(controller.getRegisterValue(register),16);
    //    int valueSign = value & 0x80;
    //    int valueMask = (valueSign << 24) >> 24;
    //    value = value | valueMask;
        int registerZero = Integer.parseInt(controller.getRegisterValue(0),16);
    //    int registerZeroSign = registerZero & 0x80;
    //    int registerMask = (registerZeroSign << 24) >> 24;
    //    registerZero = registerZero | registerMask;
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
        if ( (IP > 5 && IP < 249) && ( (IP % 2) == 0)) { // this should test firstNibble
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
        } 
        else if (IP < 5) { // got to handle the edge cases...
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
        } 
        else if (IP > 249) {
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
        } 
        else { // IP is odd... 
            update = false;
        }

        if (update) {
            // send codes off to disassembler 
            String consoleText;
            consoleText = disassembler.getConsoleDisassemble(codes, relativeIP);
            //}
            controller.setDisassText(consoleText);
        }	
    }
}
