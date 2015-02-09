/**
 * Copyright (c) 2012, 2013, Huawei Technologies Co., Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.huawei.ipr.pof.gui.console;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFGroupMod.OFGroupType;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.OFPacketIn.OFPacketInReason;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionAddField;
import org.openflow.protocol.action.OFActionCalculateCheckSum;
import org.openflow.protocol.action.OFActionCounter;
import org.openflow.protocol.action.OFActionDeleteField;
import org.openflow.protocol.action.OFActionDrop;
import org.openflow.protocol.action.OFActionDrop.OFDropReason;
import org.openflow.protocol.action.OFActionGroup;
import org.openflow.protocol.action.OFActionModifyField;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionPacketIn;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.action.OFActionSetFieldFromMetadata;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoDirectTable;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.protocol.instruction.OFInstructionMeter;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.instruction.OFInstructionWriteMetadata;
import org.openflow.protocol.instruction.OFInstructionWriteMetadataFromPacket;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.manager.IPMService;
/**
 * A command line console UI for POF Controller. User could input commands to 
 * control POF Controller and switches via this console UI.
 * 
 * <p>
 * the command format is as: <br>
 * &nbsp &nbsp &nbsp OPCMD OPTYPE PARAMETER1 PARAMETER2 ...
 * <p>
 * OPCMD include: <br>
 * &nbsp &nbsp &nbsp DISPLAY (or DIS), ADD, DELETE (or DEL), MODIFY, RESET, SET,
 * SAVE, LOAD, HELP, ABOUT, VERSION, EXIT
 * <p>
 * OPTYPE include: <br>
 * &nbsp &nbsp &nbsp PROTOCOL, METADATA, SWITCH, PORT, TABLE, ENTRY
 * (or FLOWENTRY/FLOW), GROUP, METER, COUNTER, ALL
 * 
 * 
 * <p>
 * Command format:
 * <p>
 * DISPLAY PROTOCOL			&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp 	//display all protocols
 * <br>DISPLAY PROTOCOL ALL			&nbsp &nbsp &nbsp 				//display all protocols
 * <br>DISPLAY PROTOCOL -1 			&nbsp &nbsp &nbsp &nbsp 		//display metadata
 * <br>DISPLAY PROTOCOL 2 			&nbsp &nbsp &nbsp &nbsp 		//display one protocol[id=2]
 * <br>DISPLAY METADATA 			&nbsp &nbsp &nbsp &nbsp &nbsp	//display metadata
 * 
 * <p>
 * DISPLAY SWITCH 		&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//display all switch's all information
 * <br>DISPLAY SWITCH ALL 		 	&nbsp &nbsp &nbsp &nbsp			//display all switch's all information
 * <br>DISPLAY SWITCH 11223344										//display the switch(id=11223344)'s information
 * 
 * <p>
 * DISPLAY PORT		&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//display all switches' all tables' all ports' information
 * <br>DISPLAY PORT ALL		&nbsp &nbsp &nbsp &nbsp &nbsp			//display all switches' all tables' all ports' information
 * <br>DISPLAY PORT 11223344.ALL	&nbsp &nbsp &nbsp				//display switch(id==11223344)'s all ports' information
 * <br>DISPLAY PORT 11223344.2		&nbsp &nbsp &nbsp &nbsp 		//display switch(id==11223344)'s one port(id==2)'s information
 * 
 * <p>
 * DISPLAY TABLE	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//display all switches' all tables' all flow entries
 * <br>DISPLAY TABLE ALL		&nbsp &nbsp &nbsp &nbsp &nbsp		//display all switches' all tables' all flow entries
 * <br>DISPLAY TABLE 11223344.ALL		&nbsp						//display switch(id==11223344)'s all tables' information
 * <br>DISPLAY TABLE 11223344.10		&nbsp &nbsp					//display switch(id==11223344)'s table(id==10)'s information
 * 
 * <p>
 * DISPLAY ENTRY	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp		//display all switches' all tables' all flow entries
 * <br>DISPLAY ENTRY ALL	&nbsp &nbsp &nbsp &nbsp	&nbsp &nbsp		//display all switches' all tables' all flow entries
 * <br>DISPLAY ENTRY 11223344	  &nbsp &nbsp &nbsp	&nbsp &nbsp		//display the switch(id=11223344)'s all tables' all flow entries
 * <br>DISPLAY ENTRY 11223344.ALL		&nbsp &nbsp					//display the switch(id=11223344)'s all tables' all flow entries
 * <br>DISPLAY ENTRY 11223344.10		&nbsp &nbsp &nbsp			//display the switch(id=11223344)'s table(id=10)'s all flow entries
 * <br>DISPLAY ENTRY 11223344.10.ALL								//display the switch(id=11223344)'s table(id=10)'s all flow entries
 * <br>DISPLAY ENTRY 11223344.10.2		&nbsp &nbsp					//display the switch(id=11223344)'s table(id=10)'s one flow entry(id=2)
 * 
 * <p>
 * DISPLAY GROUP	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp		//display all switches' all group entries
 * <br>DISPLAY GROUP ALL		&nbsp &nbsp &nbsp &nbsp	&nbsp &nbsp	//display all switches' all group entries
 * <br>DISPLAY GROUP 11223344	&nbsp &nbsp &nbsp &nbsp &nbsp		//display the switch(id=11223344)'s all group entries
 * <br>DISPLAY GROUP 11223344.ALL		&nbsp &nbsp					//display the switch(id=11223344)'s all group entries
 * <br>DISPLAY GROUP 11223344.3			&nbsp &nbsp &nbsp			//display the switch(id=11223344)'s group entry(id=3)
 * 
 * <p>
 * DISPLAY METER	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp		//display all switches' all meters 
 * <br>DISPLAY METER ALL		&nbsp &nbsp &nbsp &nbsp	&nbsp &nbsp	//display all switches' all meters
 * <br>DISPLAY METER 11223344	&nbsp &nbsp &nbsp &nbsp	&nbsp		//display the switch(id=11223344)'s all meters
 * <br>DISPLAY METER 11223344.ALL		&nbsp &nbsp					//display the switch(id=11223344)'s all meters 
 * <br>DISPLAY METER 11223344.5			&nbsp &nbsp &nbsp			//display the switch(id=11223344)'s meters(id=5) 
 * 
 * <p>
 * DISPLAY COUNTER 11223344.6	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//display the switch(id=11223344)'s counter's(id=6) value 
 * 
 * 
 * <p>
 * DISPLAY ALL &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp 
 * //display all information include metadata/protocols/switches/tables/entryies/gourps/meters
 * 
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>
 * ADD PROTOCOL MAC dmac.48 smac.48 etype.16 &nbsp 						//add a new protocol, with its name(MAC) and the fields (name.length)
 * <br>ADD METADATA mf1.16 mf2.8 &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp 	// add new metadata elements (name.length) on the tail of the metadata list
 * 
 * <p>
 * ADD TABLE 11223344 Acl MM 100 4.6.a1.8.3 &nbsp &nbsp	//add a new table in switch(id=11223344), 
 * 															the table is a 100 entry in maximal, 
 * 															MM (MaskedMatch) type {@link OFTableType}, with name Acl.
 * 															the table has five match fields (fields id are: 4, 6, metadata named a1, 8 and 3)
 * 
 * <p>
 * ADD ENTRY 11223344.10 0 1.0.0 3.0889.ffff INS=APPLY_ACTIONS(SET_FIELD.3.0777.ffff$CALCULATE_CHECKSUM.1.2.3.4);GOTO_TABLE.10.0;
 * <br>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	
 * 		//add a new flow entry in switch(id=11223344)'s table(id=10), 
 *			the entry's priority=0, 
 *			match_file_value1=[fieldid=1,value=0,mask=0], match_file_value2=[fieldid=3,value=0x0889,mask=0xffff],
 *			contain two instructions (use ";" to separate), 
 *					ins1 is an APPLY_ACTIONS {@link OFInstructionType}, ins2 is a GOTO_TABLE {@link OFInstructionType}, 
 *						the APPLY_ACTIONS GOTO_TABLE contains two actions (use "$" to separate), 
 *								action1 is a SET_FIELD {@link OFActionType}, with three parameters (3.0777.ffff);
 *								action2 is a CALCULATE_CHECKSUM {@link OFActionType}, with four parameters (1.2.3.4);
 *						the GOTO_TABLE instruction with two parameters (10.0)
 * 
 * <p>
 * ADD GROUP 11223344 ALL SET_FIELD.4.1111.ffff  CALCULATE_CHECKSUM.1.2.3.4  OUTPUT.5.0.0.0
 * <br>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	
 * 		//add a new group entry in switch(id=11223344), the OFGroupType {@link OFGroupType} is ALL,
 * 			the action list contains three actions: SET_FIELD, SET_FIELD, OUTPUT
 * 
 * <p>
 * ADD METER 11223344 32	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//add a new meter in switch(id=11223344) with the rate is 32 kbps
 * 
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>
 * MODIFY PROTOCOL 2 dmac.48 smac.48 etype.16 &nbsp &nbsp &nbsp //modify a existed protocol[id=2], with the new fields (name.length)
 * <br>MODIFY METADATA mf11.32 mf22.64 &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp //modify the metadata list using the new metadata elements (name.length)
 * <br>
 * 
 * <p>
 * MODIFY ENTRY 11223344.10.2 0 1.0.0 3.0889.ffff INS=APPLY_ACTIONS(SET_FIELD.3.0777.ffff$CALCULATE_CHECKSUM.1.2.3.4);APPLY_ACTIONS(OUTPUT.5.0.0.0)
 * <br>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	
 * 		//modify a existed flow entry(id=2) in switch(id=11223344)'s table(id=10), with new parameters
 * 
 * <p>
 * MODIFY GROUP 11223344.3 ALL SET_FIELD.fid.hex_v.hex_m  CALCULATE_CHECKSUM.1.2.3.4  OUTPUT.5.0.0.0
 * <br>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	
 * 		//modify a existed group entry(id=3) in switch(id=11223344), with new parameters
 * 
 * <p>
 * MODIFY METER 11223344.5 64	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp	//modify a existed meter(id=5) in switch(id=11223344) with new rate is 64 kbps
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>
 * DELETE PROTOCOL 2 &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp 	//delete a existed protocol[id=2]
 * 
 * <p>
 * DELETE TABLE 11223344.ALL	&nbsp &nbsp &nbsp 			//delete the switch(id=11223344)'s all empty tables
 * <br>DELETE TABLE 11223344.10	&nbsp &nbsp &nbsp 			//delete the switch(id=11223344)'s table(id=10), 
 * 																if the table(id=10) is not empty, it will not be deleted.
 * <p>
 * DELETE ENTRY 11223344.10.ALL		&nbsp &nbsp &nbsp		//delete the switch(id=11223344)'s table(id=10)'s all entries.
 * <br>DELETE ENTRY 11223344.10.2	&nbsp &nbsp &nbsp &nbsp	//delete the switch(id=11223344)'s table(id=10)'s one entries(id=2).
 * 
 * <p>
 * DELETE GROUP 11223344.ALL		&nbsp &nbsp &nbsp		//delete the switch(id=11223344)'s all groups.
 * <br>DELETE GROUP 11223344.3		&nbsp &nbsp &nbsp &nbsp	//delete the switch(id=11223344)'s one group(id=3).
 * 
 * <p>
 * DELETE METER 11223344.ALL		&nbsp &nbsp &nbsp		//delete the switch(id=11223344)'s all meters.
 * <br>DELETE METER 11223344.5		&nbsp &nbsp &nbsp &nbsp	//delete the switch(id=11223344)'s one meter(id=5).
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>
 * SET PORT ALL true	&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp 	//set all switches' all ports' openflow enable
 * <br>SET PORT 11223344 enable				&nbsp				//set switch(id=11223344)'s all ports' openflow enable
 * <br>SET PORT 11223344.all false			&nbsp				//set switch(id=11223344)'s all ports' openflow disable
 * <br>SET PORT 11223344.2 disable			&nbsp				//set switch(id=11223344)'s port(id=2)'s openflow disable
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br> 
 * RESET COUNTER 11223344.6	&nbsp &nbsp &nbsp &nbsp &nbsp 		//reset the switch(id=11223344)'s counter(id=5)'s value=0
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br> 
 * SAVE filename.db			&nbsp &nbsp &nbsp &nbsp &nbsp		//save database into a filename.db
 * 
 * 
 * <p>
 * <br> 
 * LOAD filename.db  yes		&nbsp &nbsp &nbsp &nbsp &nbsp	//load database from filename.db and write OFMessages to switches
 * <br>LOAD filename.db  no		&nbsp &nbsp &nbsp &nbsp &nbsp	//load database from filename.db and do NOT write OFMessages
 * <br>LOAD finename.pofscript	&nbsp &nbsp				 		//load console script from a finename.pofscript
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>
 * ABOUT			&nbsp &nbsp &nbsp &nbsp &nbsp			//print version information
 * <p>
 * <br>
 * VERSION			&nbsp &nbsp &nbsp						//print version information
 * <p>
 * <br>
 * HELP				&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp		//print help information
 * 
 * 
 * 
 * <p>
 * <br>
 * <p>
 * <br>  
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class ConsoleUI extends Thread{
	protected static Logger log = LoggerFactory.getLogger(ConsoleUI.class);
	
	public static final String CONSOLE_TITLE = "##########    POF Controller Console    ##########";
	public static final String SWITCHID_DEC_RE = GUITools.RE_PMDEC;
	public static final String INS_HEADER = "INS=";
	public static final String INS_SEPARATOR = ";";
	
	public static final String SPLIT_DOT = "\\.";
	public static final String SPLIT_DOLLAR = "[$]";	//ACTION_SEPARATOR = "$";
	
	public static final boolean CHECK_GLOBAL = true;
	
	public static final OFInstructionType[] INSTRUCTIONS = IPMService.INSTRUCTIONS;
	public static final OFActionType[] ACTIONS = IPMService.ACTIONS;
	
	protected static ConsoleUIMainPanel consoleUIMainPanel;
	
	public final static IPMService pofManager = ConsoleUIMainPanel.pofManager;
	
	protected static Map<Integer, List<OFMessage>> switchInfoMap = new ConcurrentHashMap<Integer, List<OFMessage>>();
	
	protected Console console = null;
	protected Scanner input = null;
	
	protected static int CMD_COUNTER = 0;
	
	public enum CMDSTRING{
		DIS,
		DISPLAY,
		ADD,
		DEL,
		DELETE,
		MODIFY,
		RESET,
		SET,
		SAVE,
		LOAD,
		ALL,
		PROTOCOL,
		METADATA,
		SWITCH,
		PORT,
		TABLE,
		ENTRY,
		FLOWENTRY,
		FLOW,
		GROUP,
		METER,
		COUNTER,
		HELP,
		ABOUT,
		VERSION,
		EXIT
	}
	
	@Override
	public void run() {
		try {
			launch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * launch the console, get the user input string and then invoke processCmd() to process.
	 */
	private void launch(){
		String str = null;
		Boolean processRet;
		try{
			Thread.sleep(3000);
	
			console = System.console();
			if(console != null){
				log.info("System.console start");
				println("\n" + CONSOLE_TITLE + "\n");
				print(">>");
				str = console.readLine();
				
				while(str != null){
					if(str.length() != 0){
						processRet = processCmd(str);
						if(processRet != null){
							if(processRet == true){
								CMD_COUNTER++;
							}else if(processRet == false){
								println(">>" + str);
							}
						}
					}
					print(">>");
					
					str = console.readLine();
				}
				
			}else{
				log.info("console fail");
				println("\n" + CONSOLE_TITLE + "\n");
				input = new Scanner(System.in);
				if(input != null){
					log.info("Scanner start");
					print(">>");
					str = input.nextLine();
				}
				
				while(str != null){
					if(str.length() != 0){
						processRet = processCmd(str);
						if(processRet != null){
							if(processRet == true){
								CMD_COUNTER++;
							}else if(processRet == false){
								println(">>" + str);
							}
						}
					}
					print(">>");
					
					str = input.nextLine();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			println("CmdCount = " + CMD_COUNTER);
			println("System.exit()");
			System.exit(1);
		}
	}
	
	/**
	 * delete all blanks(" ") and tabs("\t") of a string's head and tail.
	 * @param str
	 * @return a new string
	 */
	private static String deleteBlank(final String str){
		if(str == null || str.length() == 0){
			return null;
		}
		
		String subString = str;
		
		while(subString.startsWith(" ") || subString.startsWith("\t")){
			subString = subString.substring(1);
		}
		
		while(subString.endsWith(" ") || subString.endsWith("\t")){
			subString = subString.substring(0, subString.length());
		}
		
		return subString;
	}
	
	/**
	 * process user input command.
	 * @param command
	 * @return success or fail
	 */
	private Boolean processCmd(final String command){
		String[] cmdArray = command.split(" ");
				
		if(cmdArray == null || cmdArray.length == 0){
			return null;
		}
		
		List<String> cmdList = new ArrayList<String>();
		
		for(String str : cmdArray){
			if(str.length() != 0 && !str.equals(" ")){
				String string = deleteBlank(str);
				if(string != null 
						&& string.length() != 0 
						&& !string.equals(" ")){
					cmdList.add(string);
				}
			}
		}
		
		if(cmdList.size() == 0){
			return null;
		}
		
		//process level 1 cmd
		for(CMDSTRING CMD: CMDSTRING.values()){
			if(cmdList.get(0).equalsIgnoreCase(CMD.name())){
				cmdList.set(0, CMD.name());	//use upper case
				switch(CMD){
					case DIS:
					case DISPLAY:
					case ADD:
					case DEL:
					case DELETE:
					case MODIFY:
					case RESET:
					case SET:
						return processCmd(cmdList, 1);
					case SAVE:
						return save(cmdList, 1);
					case LOAD:
						return load(cmdList, 1);
					case EXIT:
						println("CmdCount = " + CMD_COUNTER);
						println("System.exit()");
						System.exit(1);
						return true;
					case ABOUT:
					case VERSION:
						displayAbout();
						return true;
					case HELP:
						displayHelp();
						return true;
						
					case ALL:
					case PROTOCOL:
					case METADATA:
					case SWITCH:
					case PORT:
					case TABLE:
					case ENTRY:
					case FLOWENTRY:
					case FLOW:
					case GROUP:
					case METER:
					case COUNTER:
					
				}
			}
		}
		
		if(cmdList.get(0).equals("?")){
			displayHelp();
			return true;
		}
		return false;
	}
	
	/**
	 * process level 2 cmd
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean processCmd(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);

		String lastCmd = cmdList.get(curCmdListIndex - 1);
		
		int nextCmdListIndex = curCmdListIndex + 1;
		
		for(CMDSTRING CMD: CMDSTRING.values()){
			if( currentCmd.equalsIgnoreCase(CMD.name()) ){
				cmdList.set(curCmdListIndex, CMD.name());	//use upper case
				switch(CMD){
					case ALL:
						if(lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name())){
							return displayAll();
						}
						break;
					case PROTOCOL:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayProtocol( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.ADD.name())){
							return addProtocol( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.MODIFY.name())){
							return modifyProtocol( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.DEL.name()) || lastCmd.equals(CMDSTRING.DELETE.name()) ){
							return delProtocol( cmdList, nextCmdListIndex );
						}
						break;
						
					case METADATA:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							displayMetadata();
							return true;
						}else if( lastCmd.equals(CMDSTRING.ADD.name()) ){
							return addMetadata( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.MODIFY.name()) ){
							return modifyMetadata( cmdList, nextCmdListIndex );
						}
						break;
						
					case SWITCH:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displaySwitch( cmdList, nextCmdListIndex );
						}
						break;
						
					case PORT:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name())){
							return displayPort( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.SET.name()) ){
							return setPort( cmdList, nextCmdListIndex );
						}
						break;
						
					case TABLE:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayTable( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.ADD.name())){
							return addTable( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.MODIFY.name()) ){
							return modifyTable( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.DEL.name()) || lastCmd.equals(CMDSTRING.DELETE.name())){
							return delEmptyTable( cmdList, nextCmdListIndex );
						}
						break;
						
					case ENTRY:
					case FLOWENTRY:
					case FLOW:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayEntry( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.ADD.name())){
							return addEntry( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.MODIFY.name()) ){
							return modifyEntry( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.DEL.name()) || lastCmd.equals(CMDSTRING.DELETE.name())){
							return delEntry( cmdList, nextCmdListIndex );
						}
						break;
						
					case GROUP:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayGroup( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.ADD.name())){
							return addGroup( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.MODIFY.name()) ){
							return modifyGroup( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.DEL.name()) || lastCmd.equals(CMDSTRING.DELETE.name())){
							return delGroup( cmdList, nextCmdListIndex );
						}
						break;
						
					case METER:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayMeter( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.ADD.name())){
							return addMeter( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.MODIFY.name()) ){
							return modifyMeter( cmdList, nextCmdListIndex );
						}else if(lastCmd.equals(CMDSTRING.DEL.name()) || lastCmd.equals(CMDSTRING.DELETE.name())){
							return delMeter( cmdList, nextCmdListIndex );
						}
						break;
						
					case COUNTER:
						if( lastCmd.equals(CMDSTRING.DIS.name()) || lastCmd.equals(CMDSTRING.DISPLAY.name()) ){
							return displayCounterValue( cmdList, nextCmdListIndex );
						}else if( lastCmd.equals(CMDSTRING.RESET.name()) ){
							return resetCounterValue( cmdList, nextCmdListIndex );
						}
						break;
				
					case EXIT:
					case DIS:
					case DISPLAY:
					case ADD:
					case DEL:
					case DELETE:
					case MODIFY:
					case RESET:
					case SET:
					case SAVE:
					case LOAD:
					case HELP:
					case ABOUT:
					case VERSION:

				}
			}
		}
		return false;
	}



	/**
	 * display all information, include: <br>
	 * . metadata and protocols; <br>
	 * . all switches; <br>
	 * . all flow tables and flow entries; <br>
	 * . all group entries; <br>
	 * . all meter entries; <br>
	 * 
	 * @return success or fail
	 */
	private boolean displayAll() {
		//metadata and protocols
		displayProtocol(null, 0);
		
		//all switches
		List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
		for(Integer switchId : allSwitchID){
			//switch id, feature, resource, ports
			displaySwitch(switchId);
			
			//tables and entries
			displayTable(switchId);
			
			//groups
			displayGroup(switchId);
			
			//meters
			displayMeter(switchId);
			
			println();
		}
		return true;
	}

	/**
	 * display protocol. <p>
	 * format: <br>
	 * 		DISPLAY PROTOCOL			//display all protocols	<br>
	 * 		DISPLAY PROTOCOL ALL		//display all protocols	<br>
	 * <br>
	 * 		DISPLAY PROTOCOL -1			//display metadata, the same as DIAPLAY METADATA
	 * 		DISPLAY PROTOCOL 2			//display protocol(id=2) information	<br>
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayProtocol(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			displayMetadata();
			
			List<OFProtocol> allProtocol = pofManager.iGetAllProtocol();
			for(OFProtocol protocol : allProtocol){
				displayProtocol(protocol);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		if(currentCmd.toUpperCase().contains("ALL")){	//display all protocols
			List<OFProtocol> allProtocol = pofManager.iGetAllProtocol();
			for(OFProtocol protocol : allProtocol){
				displayProtocol(protocol);
			}
			return true;
		}else if(currentCmd.matches(GUITools.RE_PMDEC)){			//display a protocol
			short protocolID = (short)Integer.parseInt(currentCmd);
			if(protocolID == IPMService.OFPROTOCOLID_INVALID){
				return false;
			}
			
			//metadata
			if(protocolID == -1){
				displayMetadata();
				return true;
			}
			
			OFProtocol protocol = pofManager.iGetProtocol(protocolID);
			if(protocol == null){
				printlnErr("protocol [id= " + protocolID + " does NOT exist!");
				return false;
			}

			displayProtocol(protocol);
			
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * display a protocol. <p>
	 * @param protocol
	 */
	private void displayProtocol(final OFProtocol protocol){
		println("Protocol [id= " + protocol.getProtocolId() + 
								", name= " + protocol.getProtocolName() +"]: "
											+ protocol.toString());
	}
	
	/**
	 * add a new protocol. <p>
	 * format: <br>
	 * 		ADD PROTOCOL protocolName f1Name.f1length f2Name.f2length ... fnName.fnlength <br>
	 * e.g.:<br>
	 * 		ADD PROTOCOL MAC dmac.48 smac.48 etype.16 <br>
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addProtocol(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 1){
			return false;
		}
		
		String protocolName = cmdList.get(curCmdListIndex);
		if(pofManager.iGetProtocol(protocolName) != null){
			printlnErr(protocolName + " already existed!");
			return false;
		}
		
		if(protocolName == null || protocolName.length() == 0){
			return false;
		}
		
		String fieldName;
		String fieldLengthString;
		short fieldLength;
		List<String> fieldNameList = new ArrayList<String>();
		List<Short> fieldLengthList = new ArrayList<Short>();
		int listSize = cmdList.size();
		
		//check field parameter
		for(int i = curCmdListIndex + 1; i < listSize; i++){
			String currentCmd = cmdList.get(i);
			String subString[] = currentCmd.split(SPLIT_DOT);
			
			if(subString == null || subString.length != 2){
				printlnErr("should be fieldname.fieldlength");
				return false;
			}
			
			fieldName = subString[0];	//field name
			if(fieldName == null || fieldName.isEmpty() || fieldNameList.contains(fieldName)){
				printlnErr("Wrong field name");
				return false;
			}
			
			fieldLengthString = subString[1];	//field length
			if(fieldLengthString == null || fieldLengthString.isEmpty() || !fieldLengthString.matches(GUITools.RE_DEC)){
				printlnErr("Wrong field length");
				return false;
			}
			
			fieldLength = (short)Integer.parseInt(fieldLengthString);
			
			if(fieldLength <= 0){
				printlnErr("Wrong field length");
				return false;
			}
			
			if(CHECK_GLOBAL){
				if(fieldLength > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8){
					printlnErr("field length [= " + fieldLength + "] > MAX_LENGTH [= " +  OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8 + "]");
					return false;
				}
			}
			
			fieldNameList.add(fieldName);
			fieldLengthList.add(fieldLength);
		}
		
		short fieldId;
		List<OFMatch20> fieldList = new ArrayList<OFMatch20>();
		short offset = 0;
		listSize = fieldNameList.size();
		
		for(int i = 0; i < listSize; i++){
			
			fieldId = pofManager.iNewField(fieldNameList.get(i), fieldLengthList.get(i), offset);
			OFMatch20 field = pofManager.iGetMatchField(fieldId);
			fieldList.add(field);
			
			offset += fieldLengthList.get(i);
		}
		
		short protocolId  = pofManager.iAddProtocol(protocolName, fieldList);
		
		if(protocolId == IPMService.OFPROTOCOLID_INVALID){
			printlnErr("Created new Protocol failed!");
			return false;
		}
		
		//display
		println("Created new Protocol!");
		OFProtocol protocol = pofManager.iGetProtocol(protocolId);
		displayProtocol(protocol);
		
		return true;
	}
	
	/**
	 * modify and override a existed protocol. <p>
	 * format: <br>
	 * 		MODIFY PROTOCOL protocolId f1Name.f1length f2Name.f2length ... fnName.fnlength <br>
	 * e.g.:<br>
	 *  	MODIFY PROTOCOL 2 dmac.48 smac.48 etype.16 <br>
	 * <p>
	 * if want to delete a protocol, use: <b>DELETE PROTOCOL 2</b> <br>
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyProtocol(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 1){
			return false;
		}
		
		String protocolIdString = cmdList.get(curCmdListIndex);
		if(!protocolIdString.matches(GUITools.RE_DEC)){
			return false;
		}
		
		short protocolId = (short)Integer.parseInt(protocolIdString);
		if(protocolId == IPMService.OFPROTOCOLID_INVALID
				|| pofManager.iGetProtocol(protocolId) == null){
			printlnErr("protocol [id= " + protocolId + " does NOT exist!");
			return false;
		}
		
		String fieldName;
		String fieldLengthString;
		short fieldLength;
		List<String> fieldNameList = new ArrayList<String>();
		List<Short> fieldLengthList = new ArrayList<Short>();
		int listSize = cmdList.size();
		//check field parameter
		for(int i = curCmdListIndex + 1; i < listSize; i++){
			String currentCmd = cmdList.get(i);
			String subString[] = currentCmd.split(SPLIT_DOT);
			
			if(subString == null || subString.length != 2){
				printlnErr("should be fieldname.fieldlength");
				return false;
			}
			
			fieldName = subString[0];	//field name
			if(fieldName == null || fieldName.isEmpty() || fieldNameList.contains(fieldName)){
				printlnErr("Wrong field name");
				return false;
			}
			
			fieldLengthString = subString[1];	//field length
			if(fieldLengthString == null || fieldLengthString.isEmpty() || !fieldLengthString.matches(GUITools.RE_DEC)){
				printlnErr("Wrong field length");
				return false;
			}
			
			fieldLength = (short)Integer.parseInt(fieldLengthString);
			
			if(fieldLength <= 0){
				printlnErr("Wrong field length");
				return false;
			}
			
			if(CHECK_GLOBAL){
				if(fieldLength > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8){
					printlnErr("field length [= " + fieldLength + "] > MAX_LENGTH [= " +  OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8 + "]");
					return false;
				}
			}
			
			fieldNameList.add(fieldName);
			fieldLengthList.add(fieldLength);
		}
		
		short fieldId;
		List<OFMatch20> fieldList = new ArrayList<OFMatch20>();
		short offset = 0;
		listSize = fieldNameList.size();
		
		for(int i = 0; i < listSize; i++){
			
			fieldId = pofManager.iNewField(fieldNameList.get(i), fieldLengthList.get(i), offset);
			OFMatch20 field = pofManager.iGetMatchField(fieldId);
			fieldList.add(field);
			
			offset += fieldLengthList.get(i);
		}
		
		if (!pofManager.iModifyProtocol(protocolId, fieldList)){
			printlnErr("Modify Protocol failed!");
			return false;
		}
		
		println("Modified the Protocol!");
		OFProtocol protocol = pofManager.iGetProtocol(protocolId);
		displayProtocol(protocol);
		
		return true;
	}
	
	/**
	 * delete a existed protocol. <p>
	 * format: <br>
	 *  	DELETE PROTOCOL protocolId <br>
	 * e.g.:<br>
	 *  	DELETE PROTOCOL 2 <br>
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean delProtocol(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String protocolIdString = cmdList.get(curCmdListIndex);
		if(!protocolIdString.matches(GUITools.RE_DEC)){
			return false;
		}
		
		short protocolId = (short)Integer.parseInt(protocolIdString);
		if(protocolId == IPMService.OFPROTOCOLID_INVALID
				|| pofManager.iGetProtocol(protocolId) == null){
			printlnErr("protocol [id= " + protocolId + " does NOT exist!");
			return false;
		}
		
		pofManager.iDelProtocol(protocolId);
		
		println("Deleted the Protocol!");
		
		return true;
	}
	
	/**
	 * display metadata
	 */
	private void displayMetadata(){
		String str = "";
		
		int totalLenth = 0;
		
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		
        for(OFMatch20 field : metadataList){
        	str += field.toString() + ", " ;
        	totalLenth += field.getLength();
        }
		
        str = "Metadata [id= -1, tl=" + totalLenth + "]: "
				+ "field(" + metadataList.size() + "): " + str;
        
		println( str );
	}
	
	/**
	 * add a metadata in the tail of the metadata list.<p>
	 * format: <br>
	 * 		ADD METADATA f1Name.f1length f2Name.f2length ... fnName.fnlength <br>
	 * <p>
	 * e.g.: <br>
	 * 		ADD METADATA mf1.48 mf2.48	//add two metadata elements 
	 * 									(name=mf1, length=48) and (name=mf2, length=48) 
	 * 									on the tail of the metadata list
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addMetadata(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String fieldName;
		String fieldLengthString;
		short fieldLength;
		int listSize = cmdList.size();
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		
		List<OFMatch20> tempFieldList = new ArrayList<OFMatch20>();
		//check field parameter
		for(int i = curCmdListIndex; i < listSize; i++){
			String currentCmd = cmdList.get(i);
			String subString[] = currentCmd.split(SPLIT_DOT);
			
			if(subString == null || subString.length != 2){
				printlnErr("should be metadataname.metadatalength");
				return false;
			}
			
			fieldName = subString[0];	//field name
			if(fieldName == null || fieldName.isEmpty() || metadataList.contains(fieldName)){
				printlnErr("Wrong metadata name");
				return false;
			}
			
			fieldLengthString = subString[1];	//field length
			if(fieldLengthString == null || fieldLengthString.isEmpty() || !fieldLengthString.matches(GUITools.RE_DEC)){
				printlnErr("Wrong metadata length");
				return false;
			}
			
			fieldLength = (short)Integer.parseInt(fieldLengthString);
			
			if(fieldLength <= 0){
				printlnErr("Wrong metadata length");
				return false;
			}
			
			if(CHECK_GLOBAL){
				if(fieldLength > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8){
					printlnErr("metadata length [= " + fieldLength + "] > MAX_LENGTH [= " +  OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8 + "]");
					return false;
				}
			}
			
			OFMatch20 tempField = new OFMatch20();
			tempField.setFieldId(OFMatch20.METADATA_FIELD_ID);
			tempField.setFieldName(fieldName);
			tempField.setLength(fieldLength);
			
			tempFieldList.add(tempField);
		}
		
		int offset = 0;
		
		if( metadataList.size() != 0){
			OFMatch20 lastFieldInMetadata = metadataList.get(metadataList.size() - 1);
			offset = lastFieldInMetadata.getOffset() + lastFieldInMetadata.getLength();
		}
		
		for(OFMatch20 tempField : tempFieldList){
			tempField.setOffset((short)offset);
			
			metadataList.add(tempField);
			
			offset += tempField.getLength();
		}
		
		displayMetadata();
		
		return true;
	}
	
	/**
	 * modify metadata list by using a new metadata list to instead the old list.<br> <p>
	 * format: <br>
	 * 		MODIFY METADATA f1Name.f1length f2Name.f2length ... fnName.fnlength <br>
	 * <P>
	 * e.g.: <br>
	 * 		MODIFY METADATA mf11.32 mf22.64 //the metadata list will be only has two elements
	 * 											(name=mf11, length=32) and (name=mf22, length=64),
	 * 											whatever the old metadata elements were.
	 * <P>
	 * if want to clear all metadata elements in metadata list, just use: <br>
	 * 		MODIFY METADATA
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyMetadata(final List<String> cmdList, final int curCmdListIndex) {
		pofManager.iGetMetadata().clear();
		
		//clear metadata only
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			displayMetadata();
			return true;
		}
		
		return addMetadata(cmdList, curCmdListIndex);
	}
	
	/**
	 * display switch information. <p>
	 * format: <br>
	 * 		DISPLAY SWITCH				//display all switch's all information	<br>
	 * 		DISPLAY SWITCH ALL			//display all switch's all information	<br>
	 * 		DISPLAY SWITCH 11223344		//display the switch(id=11223344)'s information	<br>
	 * <p>
	 * display information of a switch include: <br>
	 * <b>switchID, feature, flow table resource, ports information <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displaySwitch(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displaySwitch(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		if(currentCmd.toUpperCase().contains("ALL")){	//display all switches' all info
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displaySwitch(switchId);
			}
			return true;
		}else if(currentCmd.matches(SWITCHID_DEC_RE)){			//display a switch's all info
			int switchId = (int)Long.parseLong(currentCmd);
			if(!switchInfoMap.containsKey(switchId)){
				printlnErr("switch [id= " + switchId + " is NOT connected!");
				return false;
			}
			
			displaySwitch(switchId);
			
			return true;
		}else{
			printlnErr("Wrong switch id!");
			return false;
		}
	}
	
	/**
	 * display information of a switch include: <br>
	 * <b>switchID <br>
	 * feature <br>
	 * flow table resource <br>
	 * ports information <br>
	 * </b>
	 * @param switchId
	 */
	private void displaySwitch(final int switchId){
		println("Switch [id= " + switchId + "]: ");
		
		OFFeaturesReply featureReply = pofManager.iGetFeature(switchId);
		println("  FeatureReply: " + featureReply.toString());
		
		OFFlowTableResource tableResource = pofManager.iGetResourceReport(switchId);
		println("  FlowTableResource: " + tableResource.toString());
		
		List<Integer> portIdList = pofManager.iGetAllPortId(switchId);
		for(int portId : portIdList){
			OFPortStatus portStatus = pofManager.iGetPortStatus(switchId, portId);
			println("  PortStatus [id=" + portId + "]: " + portStatus.toString());
		}
	}
	
	/**
	 * display port information <br>
	 * <p>
	 * format: <br>
	 * 		DISPLAY PORT [ALL]|[SwitchID.TableId]
	 * <p>
	 * e.g.: <br>
	 * 		DISPLAY PORT					//display all switches' all tables' all ports' information <br>
	 * 		DISPLAY PORT ALL				//display all switches' all tables' all ports' information <br>
	 * <p>
	 * 		DISPLAY PORT 11223344.ALL		//display switch(id==11223344)'s all ports' information<br>
	 * 		DISPLAY PORT 11223344.2		//display switch(id==11223344)'s one port(id==2)'s information
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayPort(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayPort(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.portID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0] == null || subString[0].length() == 0){
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//display all switches's all ports
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayPort(switchId);
			}
			return true;
		}
		
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		if(subString.length <= 1){	//display the switch's all ports
			displayPort(switchId);
			return true;
		}
		
		//subString[1] is table ID
		if(subString[1] == null || subString[1].length() == 0 || subString[1].toUpperCase().contains("ALL")){
			displayPort(switchId);	//display the switch's all tables
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			return false;
		}

		int portId = Integer.parseInt(subString[1]);
		
		displayPort(switchId, portId);
		
		return true;
	}
	
	/**
	 * display all displayPort information of a switch <br>
	 * @param switchId
	 */
	private void displayPort(final int switchId){
		List<Integer> portIdList = pofManager.iGetAllPortId(switchId);
		if(portIdList != null && portIdList.size() != 0){
			println("Switch [id= " + switchId + "]: ");
			for(int portId : portIdList){
				displayPort(switchId, portId);
			}
		}
	}
	
	/**
	 * display port information <br>
	 * @param switchId
	 * @param portId
	 */
	private void displayPort(final int switchId, final int portId){
		OFPortStatus portStatus = pofManager.iGetPortStatus(switchId, portId);
		if(portStatus != null){
			println("  PortStatus [id=" + portId + "]: " + portStatus.toString());
		}else{
			printlnErr(" port[id= " + portId + "] in switch[id= " + switchId + " does NOT exist");
		}
	}
	
	/**
	 * set port's openflow enable/disable
	 * <p>
	 * format: <br>
	 * 		SET PORT switchId.portId true/enable/false/disable
	 * <p>
	 * e.g.: <br>
	 * 		SET PORT 11223344.2 true	//set switch(id=11223344)'s port(id=2)'s openflow enable 	<br>
	 * 		SET PORT 11223344.2 enable	//set switch(id=11223344)'s port(id=2)'s openflow enable	<br>
	 * 		SET PORT 11223344.2 false	//set switch(id=11223344)'s port(id=2)'s openflow disable 	<br>
	 * 		SET PORT 11223344.2 disable	//set switch(id=11223344)'s port(id=2)'s openflow disable	<br>
	 * 	
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean setPort(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 1){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);

		//subString[0].subString[1].subString[2]
		//switchID.portID.true
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null || subString[0].length() == 0 ){
			return false;
		}
		
		int switchId = IPMService.SWITCHID_INVALID;
		if(!subString[0].toUpperCase().contains("ALL")){
			if(!subString[0].matches(SWITCHID_DEC_RE)){
				printlnErr("Wrong switch id!");
				return false;
			}
			
			switchId = (int)Long.parseLong(subString[0]);
			if(!switchInfoMap.containsKey(switchId)){
				printlnErr("switch [id= " + switchId + " is NOT connected!");
				return false;
			}
		}
		

		
		//true/enable/false/disable
		currentCmd = cmdList.get(curCmdListIndex + 1);
		if(currentCmd.length() == 0){
			return false;
		}
		
		currentCmd = currentCmd.toUpperCase();
		byte onoff;
		if(currentCmd.contains("TRUE") 
				|| currentCmd.contains("ENABLE")){
			onoff = 1;
		}else if(currentCmd.contains("FALSE") 
					|| currentCmd.contains("DISABLE")){
			onoff = 0;
		}else{
			printlnErr("please use TRUE or ENABLE to make the port openflow enable, FLASE or DISABLE to make the port openflow disable");
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//set all switches' all ports
			List<Integer> switchIdList = pofManager.iGetAllSwitchID();
			for(int switchID : switchIdList){
				List<Integer> portIdList = pofManager.iGetAllPortId(switchID);
				for(int portId : portIdList){
					pofManager.iSetPortOpenFlowEnable(switchID, portId, onoff);
				}
			}
			
			for(int switchID : switchIdList){
				displayPort(switchID);
			}
			
			return true;
		}else{
			List<Integer> portIdList = pofManager.iGetAllPortId(switchId);
			if(subString.length == 1 
					|| (subString.length == 2 && subString[1].toUpperCase().contains("ALL"))){	//set the switch's all ports
				for(int portId : portIdList){
					pofManager.iSetPortOpenFlowEnable(switchId, portId, onoff);
				}
				displayPort(switchId);
				return true;
			}else if(subString.length == 2){
				if(!subString[1].matches(GUITools.RE_DEC)){
					printlnErr("Wrong port id");
					return false;
				}

				int portId = Integer.parseInt(subString[1]);
				if(!portIdList.contains(portId)){
					printlnErr(" port[id= " + portId + "] in switch[id= " + switchId + " does NOT exist");
					return false;
				}
				
				//set the switch's a port
				pofManager.iSetPortOpenFlowEnable(switchId, portId, onoff);
				
				displayPort(switchId, portId);
				return true;
			}else{
				return false;
			}
		}

	}
	

	/**
	 * display flow table <br>
	 * <p>
	 * format: <br>
	 * 		DISABLE TABLE [ALL]|[SwitchID.TableId]
	 * <p>
	 * e.g.: <br>
	 * 		DISPLAY TABLE					//display all switches' all tables' all flow entries <br>
	 * 		DISPLAY TABLE ALL				//display all switches' all tables' all flow entries <br>
	 * <p>
	 * 		DISPLAY TABLE 11223344.ALL		//display switch(id==11223344)'s all tables' information,<br>
	 * 		DISPLAY TABLE 11223344.10		//display switch(id==11223344)'s table(id==10)'s information,
	 * 								if b is empty, display all tables' information in switch.a
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayTable(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayTable(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.tableID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0] == null || subString[0].length() == 0){
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//display all switches's all tables
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayTable(switchId);
			}
			return true;
		}
		
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		if(subString.length <= 1){	//display the switch's all tables
			displayTable(switchId);
			return true;
		}
		
		//subString[1] is table ID
		if(subString[1] == null || subString[1].length() == 0 || subString[1].toUpperCase().contains("ALL")){
			displayTable(switchId);	//display the switch's all tables
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id");
			return false;
		}

		byte globalTableId = (byte)Integer.parseInt(subString[1]);
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);
		if(null == ofFlowTable){	
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		//display the table ID
		displayTable(switchId, ofFlowTable);
		
		return true;
	}
	
	/**
	 * display a switch's all flow table information
	 * @param switchId
	 */
	private void displayTable(final int switchId){
		List<OFFlowTable> flowTableList = pofManager.iGetAllFlowTable(switchId);
		if(flowTableList != null && flowTableList.size() != 0){
			println("Switch [id= " + switchId + "]: ");
			for(OFFlowTable flowTable : flowTableList){
				displayTable(switchId, flowTable);
			}
		}
	}
	
	/**
	 * display a flow table information 
	 * and all flow entries in this table
	 * @param switchId
	 * @param ofFlowTable
	 */
	private void displayTable(final int switchId, final OFFlowTable ofFlowTable){
		if(ofFlowTable == null){
			return;
		}
		
		byte globalTableId = pofManager.parseToGlobalTableId(switchId, ofFlowTable.getTableType().getValue(), ofFlowTable.getTableId());
		int entryNumber = pofManager.iGetFlowEntryNumber(switchId, globalTableId);
		
		//display flow table information
		println("  Table [gtid= " + globalTableId + "]: (entryNum=" + entryNumber + ") " + ofFlowTable.toString());
		
		//TODO display the flow entry or not?
		//display all flow entries
		List<OFFlowMod> ofFlowModList = pofManager.iGetAllFlowEntry(switchId, globalTableId);
		for(OFFlowMod ofFlowMod : ofFlowModList){
			println("     Entry [fid= " + ofFlowMod.getIndex() + "]: " + ofFlowMod.toString());
		}
	}
	
	/**
	 * add a new table. <p>
	 * format: <br>
	 * 		ADD TABLE switchid tableName tableType tableSize f1id.f2id.metadataName.f3id.f4id
	 * <p>
	 * e.g.:<br>
	 * 		ADD TABLE 11223344 Acl MM 100 4.6.a1.8.3 	//add a new table in switch(id=11223344), 
	 * 														the table is a 100 entry in maximal, 
	 * 														MM (MaskedMatch) type, with name Acl.
	 * 														the table has five match fields (fields id are: 4, 6, metadata named a1, 8 and 3)<br>
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addTable(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 3){
			return false;
		}
		
		int switchId;
		String tableName;
		byte tableType;
		short keyLength = 0;
		int tableSize;
		List<OFMatch20> matchFieldList = new ArrayList<OFMatch20>();;
		
		//1. switch id
		String currentCmd = cmdList.get(curCmdListIndex);
		if(!currentCmd.matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(currentCmd);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//2. tableName
		tableName = cmdList.get(curCmdListIndex + 1);
		if( 0 == pofManager.iGetAllTableNumber(switchId) 
				&& !tableName.equalsIgnoreCase( IPMService.FIRST_ENTRY_TABLE_NAME )){
			printlnErr("first table name should be \"" + IPMService.FIRST_ENTRY_TABLE_NAME +"\"");
			tableName = IPMService.FIRST_ENTRY_TABLE_NAME;
		}
		
		//3. tableType
		currentCmd = cmdList.get(curCmdListIndex + 2);
		currentCmd = currentCmd.toUpperCase();
		if(currentCmd.contains("LINEAR") || currentCmd.contains("DT")){
			tableType = OFTableType.OF_LINEAR_TABLE.getValue();
		}else{
			if(currentCmd.contains("MM")){
				tableType = OFTableType.OF_MM_TABLE.getValue();
			}else if(currentCmd.contains("LPM")){
				tableType = OFTableType.OF_LPM_TABLE.getValue();
			}else if(currentCmd.contains("EM")){
				tableType = OFTableType.OF_EM_TABLE.getValue();
			}else{
				printlnErr("Wrong table type.");
				return false;
			}
			
			if(cmdList.size() <= curCmdListIndex + 4){
				return false;
			}
		}
		
		//FIRST_ENTRY_TABLE_NAME can be MM table only (because FIRST_ENTRY_TABLE_NAME's globalTableId must be 0)
		if(tableName.equalsIgnoreCase( IPMService.FIRST_ENTRY_TABLE_NAME )
				&& tableType != OFTableType.OF_MM_TABLE.getValue()){
			printlnErr(IPMService.FIRST_ENTRY_TABLE_NAME + " can be MM table only");
			return false;
		}

		
		//4. tableSize
		currentCmd = cmdList.get(curCmdListIndex + 3);
		if(!currentCmd.matches(GUITools.RE_DEC)){
			printlnErr("Wrong table size.");
			return false;
		}
		tableSize = (int)Long.parseLong(currentCmd);
		if(tableSize <= 0){
			return false;
		}
		
		if(tableType != OFTableType.OF_LINEAR_TABLE.getValue()){
			//5. fields
			currentCmd = cmdList.get(curCmdListIndex + 4);
			String subString[] = currentCmd.split(SPLIT_DOT);
			
			if(subString == null || subString.length == 0){
				return false;
			}
			
			short fieldId;
			
			
			for(String fieldIdString : subString){
				OFMatch20 matchField = null;
				
				if(!fieldIdString.matches(GUITools.RE_DEC)){
					//check metadata
					List<OFMatch20> metadataList = pofManager.iGetMetadata();
					for(OFMatch20 metadataField : metadataList){
						if(metadataField.getFieldName().equalsIgnoreCase(fieldIdString)){
							matchField = metadataField;
							
							break;
						}
					}
					if(matchField == null){
						printlnErr("metadata [name= " + fieldIdString + " does NOT exist!");
						return false;
					}
				}else{
					fieldId = (short)Integer.parseInt(fieldIdString);
					if(fieldId == IPMService.FIELDID_INVALID){
						return false;
					}
					matchField = pofManager.iGetMatchField(fieldId);
					if(matchField == null){
						printlnErr("field [id= " + fieldId + " does NOT exist!");
						return false;
					}
				}
				
				keyLength += matchField.getLength();
				matchFieldList.add(matchField);
			}
			
			if(matchFieldList.size() == 0){
				printlnErr("no match field!");
				return false;
			}
		}
		
		if(CHECK_GLOBAL){
			if(matchFieldList.size() > OFGlobal.OFP_MAX_MATCH_FIELD_NUM){
				printlnErr("Match field number [= " + matchFieldList.size() + "] is > MAX[= " + OFGlobal.OFP_MAX_MATCH_FIELD_NUM + "]");
				return false;
			}
		}

		
	    byte globalTableId = pofManager.iAddFlowTable(switchId, tableName, tableType, keyLength, tableSize,
	    												(byte)matchFieldList.size(), matchFieldList);
	    
	    if(globalTableId == IPMService.FLOWTABLEID_INVALID){
	    	printlnErr("create new table failed!");
	    	return false;
	    }
		
		println("Created new table!");
		this.displayTable(switchId, pofManager.iGetFlowTable(switchId, globalTableId));
		return true;
	}
	
	/**
	 * Do NOT support yet
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyTable(final List<String> cmdList, final int curCmdListIndex) {
		printlnErr("Do NOT support modify table, please delete it and create a new one.");
		return false;
	}

	/**
	 * delete a existed protocol. if the table is NOT emtry, need user to delete the entries first. 
	 * <p>
	 * format: <br>
	 * 		DELETE TABLE SwitchId.[TableId|ALL]
	 * <p>
	 * e.g.:<br>
	 * 		DELETE TABLE 11223344.ALL	//delete the switch(id=11223344)'s all empty tables <br>
	 * 		DELETE TABLE 11223344.10	//delete the switch(id=11223344)'s table(id=10), if the table(id=10) is not empty, it will not be deleted.
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean delEmptyTable(List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.tableID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0].length() == 0 || !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}

			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is table ID
		if(subString[1].length() == 0){
			return false;
		}
		
		byte globalTableId;
		if(subString[1].toUpperCase().contains("ALL")){	//delete all empty flow table
			List<OFFlowTable> flowTableList = pofManager.iGetAllFlowTable(switchId);
			if(flowTableList != null){
				for(OFFlowTable flowTable : flowTableList){
					globalTableId = pofManager.parseToGlobalTableId(switchId, flowTable.getTableType().getValue(), flowTable.getTableId());
					pofManager.iDelEmptyFlowTable(switchId, globalTableId);
				}
			}
			
			println("Deleted all empty tables. \n Now the tables in the switch: ");
			
			displayTable(switchId);
			
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id");
			return false;
		}

		globalTableId = (byte)Integer.parseInt(subString[1]);
		
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);
		if(null == ofFlowTable){
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		if(false == pofManager.iDelEmptyFlowTable(switchId, globalTableId)){
			printlnErr("Delete failed. Table is NOT empty? Please delete all flow entries first. (use DELETE ENTRY switchID.TableID.ALL)");
			return false;
		}
		
		return true;
	}
	
	/**
	 * display flow entry <br>
	 * <p>
	 * format: <br>
	 * 		DISPLAY ENTRY [SwitchId|ALL][.TableID|.ALL][.EntryID|.ALL]
	 * <p>
	 * e.g.:<br>
	 * 		DISPLAY ENTRY					//display all switches' all tables' all flow entries <br>
	 * 		DISPLAY ENTRY ALL				//display all switches' all tables' all flow entries <br>
	 * <p>
	 * 		DISPLAY ENTRY 11223344			//display the switch(id=11223344)'s all tables' all flow entries <br>
	 * 		DISPLAY ENTRY 11223344.ALL		//display the switch(id=11223344)'s all tables' all flow entries <br>
	 * <p>
	 * 		DISPLAY ENTRY 11223344.10		//display the switch(id=11223344)'s table(id=10)'s all flow entries <br>
	 * 		DISPLAY ENTRY 11223344.10.ALL	//display the switch(id=11223344)'s table(id=10)'s all flow entries <br>
	 * <p>
	 * 		DISPLAY ENTRY 11223344.10.2		//display the switch(id=11223344)'s table(id=10)'s one flow entry(id=2) <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayEntry(final List<String> cmdList, final int curCmdListIndex){
		if(cmdList == null || cmdList.size() <= curCmdListIndex){//display all switches' all tables' all flow entries
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayEntry(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1].subString[2]
		//switchID.tableID.entryID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null || subString[0].length() == 0){
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//display all switches' all tables' all flow entries
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayTable(switchId);
			}
			return true;
		}
		
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		if(subString.length <= 1){	//if only have switch id, display a switch's all tables' all flow entries
			displayEntry(switchId);
			return true;
		}
		
		//subString[1] is table ID
		if(subString[1] == null || subString[1].length() == 0 || subString[1].toUpperCase().contains("ALL")){
			//if only have switch id, display a switch's all tables' all flow entries
			displayEntry(switchId);
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id");
			return false;
		}

		byte globalTableId = (byte)Integer.parseInt(subString[1]);
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);
		if(null == ofFlowTable){
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		
		if(subString.length <= 2){
			//if only have switch id and table id, display the switch(id==subString[0])'s table(id==subString[1]'s all flow entries
			displayEntry(switchId, ofFlowTable);
			return true;
		}
		
		//subString[2] is flow entry ID
		if(subString[2] == null || subString[2].length() == 0 || subString[2].toUpperCase().contains("ALL")){
			//if only have switch id and table id, display the switch(id==subString[0])'s table(id==subString[1]'s all flow entries
			displayEntry(switchId, ofFlowTable);
			return true;
		}
		
		if(!subString[2].matches(GUITools.RE_DEC)){
			printlnErr("Wrong entry id");
			return false;
		}
		
		//if only have switch id, table id and flow entry id, 
		//display the switch(id==subString[0])'s table(id==subString[1])'s flow entry(id==subString[2]) information
		int flowEntryId = Integer.parseInt(subString[2]);
		if(flowEntryId == IPMService.FLOWENTRYID_INVALID){
			return false;
		}
		displayEntry(switchId, globalTableId, flowEntryId);
		
		return true;
	}
	
	/**
	 * display a switch's all flow tables' all flow entries' information
	 * @param switchId
	 */
	private void displayEntry(final int switchId){
		List<OFFlowTable> flowTableList = pofManager.iGetAllFlowTable(switchId);
		if(flowTableList != null && flowTableList.size() != 0){
			println("Switch [id= " + switchId + "]: ");
			for(OFFlowTable flowTable : flowTableList){
				displayEntry(switchId, flowTable);
			}
		}
	}
	
	/**
	 * display a flow table's all flow entries' information
	 * @param switchId
	 * @param ofFlowTable
	 */
	private void displayEntry(final int switchId, final OFFlowTable ofFlowTable){
		if(ofFlowTable != null){
			byte globalTableId = pofManager.parseToGlobalTableId(switchId, ofFlowTable.getTableType().getValue(), ofFlowTable.getTableId());
			
			List<OFFlowMod> ofFlowModList = pofManager.iGetAllFlowEntry(switchId, globalTableId);
			if(ofFlowModList != null){
				println("  Table [tid= " + globalTableId + "]: (entryNum=" + ofFlowModList.size() + ") ");
				for(OFFlowMod ofFlowMod : ofFlowModList){
					println("    Entry [fid= " + ofFlowMod.getIndex() + "]: " + ofFlowMod.toString());
				}
			}
		}
	}
	
	/**
	 * display a flow entry's information
	 * @param switchId
	 * @param globalTableId
	 * @param flowEntryId
	 */
	private void displayEntry(final int switchId, final byte globalTableId, final int flowEntryId){
		OFFlowMod ofFlowMod = pofManager.iGetFlowEntry(switchId, globalTableId, flowEntryId);
		if(ofFlowMod != null){
			println("    Entry [fid= " + ofFlowMod.getIndex() + "]: " + ofFlowMod.toString());
		}
	}

	/**
	 * parse a string like "fid.hex_value.hex_mask" into a OFMatchX
	 * @param cmdString
	 * @return an OFMatchX with parameters
	 */
	private OFMatchX parseToMatchX(final String cmdString){
		if(cmdString == null){
			return null;
		}
		String subString[] = cmdString.split(SPLIT_DOT);
		
		if(subString == null || subString.length != 3){
			return null;
		}
		
		short fieldId;
		OFMatchX matchX = null;
		
		//field id (or metadata name)
		if(!subString[0].matches(GUITools.RE_DEC)){
			//check metadata
			List<OFMatch20> metadataList = pofManager.iGetMetadata();
			for(OFMatch20 metadataField : metadataList){
				if(metadataField.getFieldName().equalsIgnoreCase(subString[0])){
					matchX = new OFMatchX(metadataField, null, null);
					
					break;
				}
			}
			if(matchX == null){
				printlnErr("metadata [name= " + subString[0] + " NOT exist");
				return null;
			}
		}else{	//field in protocol
			fieldId = (short)Integer.parseInt(subString[0]);
			if(fieldId == IPMService.FIELDID_INVALID){
				return null;
			}
			OFMatch20 matchField = pofManager.iGetMatchField(fieldId);
			if(matchField == null){
				printlnErr("field [id= " + fieldId + " NOT exist");
				return null;
			}
			
			matchX = new OFMatchX(matchField, null, null);
		}
		
		if(subString[1].matches(GUITools.RE_0xHEX)){
			subString[1] = subString[1].substring(2);
		}
		
		if(subString[2].matches(GUITools.RE_0xHEX)){
			subString[2] = subString[1].substring(2);
		}
		
		if(!subString[1].matches(GUITools.RE_HEX)
				|| !subString[2].matches(GUITools.RE_HEX)){
			printlnErr("value/mask [" + subString[1] + "/" + subString[2] + "] error");
			return null;
		}
		
		//hex_value
		byte[] valueBytes = GUITools.parseTextToHexBytes(null, "Value", subString[1], matchX.getLength());
		if(valueBytes == null){
			printlnErr("value [" + subString[1] + "] error");
			return null;
		}
		
		//hex_mask
		byte[] maskBytes = GUITools.parseTextToHexBytes(null, "Mask", subString[2], matchX.getLength());
		if(maskBytes == null){
			printlnErr("mask [" + subString[2] + "] error");
			return null;
		}
		
		matchX.setValue(valueBytes);
		matchX.setMask(maskBytes);
		
		return matchX;
	}
	
	private boolean parseToInstructionList(String currentCmd, final int switchId, List<OFInstruction> insList){
		if(currentCmd == null || !currentCmd.startsWith(INS_HEADER)){
			printlnErr("should start with " + INS_HEADER);
			return false;
		}
		currentCmd = currentCmd.substring(INS_HEADER.length());
		
		//split ins
		String insString[] = currentCmd.split(INS_SEPARATOR);
		if(insString == null || insString.length == 0){
			printlnErr("NO instruction!");
			return false;
		}
		
		try{
			for(String curIns : insString){
				boolean hitInsFlag = false;
				for(OFInstructionType insType : INSTRUCTIONS){
					if(curIns.toUpperCase().startsWith(insType.name())){
						OFInstruction ins = null;
						hitInsFlag = true;
						
						//use startsWith make WRITE_METADATA_FROM_PACKET match WRITE_METADATA, 
						//so here do a special processing
						if(curIns.toUpperCase().contains(OFInstructionType.WRITE_METADATA_FROM_PACKET.name())){
							insType = OFInstructionType.WRITE_METADATA_FROM_PACKET;
						}else if(curIns.toUpperCase().contains(OFInstructionType.WRITE_METADATA.name())){
							insType = OFInstructionType.WRITE_METADATA;
						}
						
						curIns = curIns.substring(insType.name().length());
						if(!curIns.isEmpty()){
							curIns = curIns.substring(1);
						}
						
						switch(insType){
							case GOTO_TABLE:
								ins = parseToInstruction(new OFInstructionGotoTable(), curIns, switchId);
								break;
							case GOTO_DIRECT_TABLE:
								ins = parseToInstruction(new OFInstructionGotoDirectTable(), curIns, switchId);
								break;
							case METER:
								ins = parseToInstruction(new OFInstructionMeter(), curIns, switchId);
								break;
							case WRITE_METADATA:
								ins = parseToInstruction(new OFInstructionWriteMetadata(), curIns);
								break;
							case WRITE_METADATA_FROM_PACKET:
								ins = parseToInstruction(new OFInstructionWriteMetadataFromPacket(), curIns);
								break;
							case APPLY_ACTIONS:
								curIns = curIns.substring(0, curIns.length() - 1);
								ins = parseToInstruction(new OFInstructionApplyActions(), curIns, switchId);
								break;
							default:
								return false;
						}
	
						if(ins == null){
							return false;
						}
						
						insList.add(ins);
						break;
					}
				}
				
				if(hitInsFlag == false){
					printlnErr("Wrong instruction type");
					return false;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if(insList.size() == 0){
			printlnErr("ins list is empty");
			return false;
		}
		
		if(CHECK_GLOBAL){
			if(insList.size() > OFGlobal.OFP_MAX_INSTRUCTION_NUM){
				printlnErr("Instruction number[= " + insList.size() + "] > MAX[= " + OFGlobal.OFP_MAX_INSTRUCTION_NUM + "]");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * add a new entry. <p>
	 * 	format: <br>
	 *  	ADD  ENTRY  switchid.globalTableId  priority  
	 *  		f1id.hex_value.hex_mask  f2id.hex_v.hex_m  metadataName.hex_v.hex_m ... fnid.hex_v.hex_m
	 *  		INS=OFInstructionType1.para1.para2.para3;OFInstructionType2.para1.para2.para3;... 
	 *  <p>
	 *  		if instruction is an action, 
	 *  			OFInstructionType2.para1.para2.para3 part should be: 
	 *  			APPLY_ACTIONS(OFActionType1.para1.para2.para3$OFActionType2.para1.para2.para3)
	 *  
	 *  <p>
	 *  Use ";" to separate instructions; <br>
	 *  Use "$" to separate actions;
	 *  <p>
	 *  e.g.:<br>
	 *  ADD ENTRY 11223344.10 0 1.0.0 3.0889.ffff 
	 *  						INS=APPLY_ACTIONS(SET_FIELD.fid.hex_v.hex_m$CALCULATE_CHECKSUM.1.2.3.4);GOTO_TABLE.10.0;APPLY_ACTIONS(OUTPUT.5.0.0.0) <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addEntry(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 2){
			return false;
		}
		
		int switchId;
		byte globalTableId;
		short priority;
		int currentCmdIndex = curCmdListIndex;
		List<OFMatchX> matchXList = new ArrayList<OFMatchX>();
		List<OFInstruction> insList = new ArrayList<OFInstruction>();
		
		//1. switchid.globalId
		String currentCmd = cmdList.get(currentCmdIndex);
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null 
				|| subString[0].length() == 0
				|| !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is globalTableId
		if(subString[1] == null 
				|| subString[1].length() == 0
				|| !subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id!");
			return false;
		}
		globalTableId = (byte)Integer.parseInt(subString[1]);
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);

		if(ofFlowTable == null){
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		
		//2. priority
		currentCmdIndex++;
		currentCmd = cmdList.get(currentCmdIndex);
		if(!currentCmd.matches(GUITools.RE_DEC)){
			printlnErr("Wrong priority value!");
			return false;
		}
		priority = (short)Integer.parseInt(currentCmd);

		//3. matchX
		if(ofFlowTable.getTableType() != OFTableType.OF_LINEAR_TABLE){
			int matchFieldNum = ofFlowTable.getMatchFieldNum();
			List<OFMatch20> matchFieldList = ofFlowTable.getMatchFieldList();
			for(int i = 0; i < matchFieldNum; i++){
				currentCmdIndex++;
				currentCmd = cmdList.get(currentCmdIndex);
				OFMatchX matchX = parseToMatchX(currentCmd);
				if(matchX == null){
					printlnErr("parse matchX error");
					return false;
				}
				
				OFMatch20 matchFieldInTable = matchFieldList.get(i);
				if(matchX.getFieldId() == OFMatch20.METADATA_FIELD_ID){
					if(matchFieldInTable.getFieldId() != OFMatch20.METADATA_FIELD_ID
							|| !matchFieldInTable.getFieldName().equalsIgnoreCase( matchX.getFieldName() ) ){
						printlnErr("matchX[" + i + "] should be metadata[name= " + matchFieldInTable.getFieldName() + "]");
						return false;
					}
				}else{
					if(matchFieldInTable.getFieldId() != matchX.getFieldId()){
						printlnErr("matchX[" + i + "] should be field[id= " + matchFieldInTable.getFieldId() + "]");
						return false;
					}
				}
				
				matchXList.add(matchX);
			}
			
			if(matchXList.size() != ofFlowTable.getMatchFieldNum()){
				printlnErr("matchX number should be " + ofFlowTable.getMatchFieldNum());
				return false;
			}
		}

		
		//4. instruction
		currentCmdIndex++;
		currentCmd = cmdList.get(currentCmdIndex);
		if(false == parseToInstructionList(currentCmd, switchId, insList)){
			return false;
		}
		
		
	    int flowEntryId = pofManager.iAddFlowEntry(switchId, 
										    		globalTableId,
										    		(byte)matchXList.size(), 
										    		matchXList,
										    		(byte)insList.size(), 
										    		insList,
									                priority);
	    
	    if(flowEntryId == IPMService.FLOWENTRYID_INVALID){
	    	println("Create flow failed");
	    	return false;
	    }
		
		println("Created new flow entry!");
		this.displayEntry(switchId, globalTableId, flowEntryId);
		return true;
	}
	
	/**
	 * modify a flow entry
	 * <p>
	 * format: <br>
	 * 		MODIFY ENTRY SwitchId.TableId.[EntryId|ALL] priority  
	 *  				f1id.hex_value.hex_mask  f2id.hex_v.hex_m  metadataName.hex_v.hex_m ... fnid.hex_v.hex_m
	 *  				INS=OFInstructionType1.para1.para2.para3;OFInstructionType2.para1.para2.para3;... 
	 *  <p>
	 *  			if instruction is an action, 
	 *  				OFInstructionType2.para1.para2.para3 part should be: 
	 *  				APPLY_ACTIONS(OFActionType1.para1.para2.para3$OFActionType2.para1.para2.para3)
	 *  
	 *  <p>
	 *  Use ";" to separate instructions; <br>
	 *  Use "$" to separate actions;
	 *  <p>
	 *  e.g.:<br>
	 *  	MODIFY ENTRY 11223344.10.2 0 1.0.0 3.0889.ffff 
	 *  								INS=APPLY_ACTIONS(SET_FIELD.fid.hex_v.hex_m$CALCULATE_CHECKSUM.1.2.3.4);GOTO_TABLE.10.0;APPLY_ACTIONS(OUTPUT.5.0.0.0) <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyEntry(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 2){
			return false;
		}
		
		int switchId;
		byte globalTableId;
		int flowEntryId;
		short priority;
		int currentCmdIndex = curCmdListIndex;
		List<OFMatchX> matchXList = new ArrayList<OFMatchX>();
		List<OFInstruction> insList = new ArrayList<OFInstruction>();
		
		//1. switchid.globalId.entryId
		String currentCmd = cmdList.get(currentCmdIndex);
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 3){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null 
				|| subString[0].length() == 0
				|| !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is globalTableId
		if(subString[1] == null 
				|| subString[1].length() == 0
				|| !subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id!");
			return false;
		}
		globalTableId = (byte)Integer.parseInt(subString[1]);
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);

		if(ofFlowTable == null){
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		
		//subString[2] is flowEntryId
		if(subString[2] == null 
				|| subString[2].length() == 0
				|| !subString[2].matches(GUITools.RE_DEC)){
			printlnErr("Wrong entry id!");
			return false;
		}
		flowEntryId = Integer.parseInt(subString[2]);
		if(flowEntryId == IPMService.FLOWENTRYID_INVALID){
			return false;
		}
		OFFlowMod ofFlowMod = pofManager.iGetFlowEntry(switchId, globalTableId, flowEntryId);

		if(ofFlowMod == null){
			printlnErr("flow entry [id= " + flowEntryId + " does NOT exist!");
			return false;
		}
		
		//2. priority
		currentCmdIndex++;
		currentCmd = cmdList.get(currentCmdIndex);
		if(!currentCmd.matches(GUITools.RE_DEC)){
			printlnErr("Wrong priority value!");
			return false;
		}
		priority = (short)Integer.parseInt(currentCmd);

		//3. matchX
		if(ofFlowTable.getTableType() != OFTableType.OF_LINEAR_TABLE){
			int matchFieldNum = ofFlowTable.getMatchFieldNum();
			List<OFMatch20> matchFieldList = ofFlowTable.getMatchFieldList();
			for(int i = 0; i < matchFieldNum; i++){
				currentCmdIndex++;
				currentCmd = cmdList.get(currentCmdIndex);
				OFMatchX matchX = parseToMatchX(currentCmd);
				if(matchX == null){
					return false;
				}
				
				OFMatch20 matchFieldInTable = matchFieldList.get(i);
				if(matchX.getFieldId() == OFMatch20.METADATA_FIELD_ID){
					if(matchFieldInTable.getFieldId() != OFMatch20.METADATA_FIELD_ID
							|| !matchFieldInTable.getFieldName().equalsIgnoreCase( matchX.getFieldName() ) ){
						printlnErr("matchX[" + i + "] should be metadata[name= " + matchFieldInTable.getFieldName() + "]");
						return false;
					}
				}else{
					if(matchFieldInTable.getFieldId() != matchX.getFieldId()){
						printlnErr("matchX[" + i + "] should be field[id= " + matchFieldInTable.getFieldId() + "]");
						return false;
					}
				}
				
				matchXList.add(matchX);
			}
			
			if(matchXList.size() != ofFlowTable.getMatchFieldNum()){
				printlnErr("matchX number should be " + ofFlowTable.getMatchFieldNum());
				return false;
			}
		}

		
		//4. instruction
		currentCmdIndex++;
		currentCmd = cmdList.get(currentCmdIndex);
		if(false == parseToInstructionList(currentCmd, switchId, insList)){
			return false;
		}
		
		
	    boolean succ =  pofManager.iModFlowEntry(switchId, 
										    		globalTableId,
										    		flowEntryId,
										    		(byte)matchXList.size(), 
										    		matchXList,
										    		(byte)insList.size(), 
										    		insList,
									                priority);
	    
	    if(succ == false){
	    	println("Modify failed, please check the parameters");
	    	return false;
	    }
		
		println("Successfully modified the flow entry!");
		this.displayEntry(switchId, globalTableId, flowEntryId);
		return true;
	}

	/**
	 * delete a flow entry
	 * <p>
	 * format: <br>
	 * 		DELETE ENTRY SwitchId.TableId.[EntryId|ALL]
	 * <p>
	 * e.g.: <br>
	 * 		DELETE ENTRY 11223344.10.ALL	//delete the switch(id=11223344)'s table(id=10)'s all entries. <br>
	 * 		DELETE ENTRY 11223344.10.2		//delete the switch(id=11223344)'s table(id=10)'s one entries(id=2). <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean delEntry(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1].subString[3]
		//switchID.tableID.entryID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 3){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0].length() == 0 || !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}

			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is table ID
		if(subString[1].length() == 0){
			return false;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong table id!");
			return false;
		}

		byte globalTableId = (byte)Integer.parseInt(subString[1]);
		
		if(globalTableId == IPMService.FLOWTABLEID_INVALID){
			return false;
		}
		
		OFFlowTable ofFlowTable = pofManager.iGetFlowTable(switchId, globalTableId);
		if(null == ofFlowTable){
			printlnErr("table [id= " + globalTableId + " does NOT exist!");
			return false;
		}
		
		//subString[2] is entryID/entryIndex
		if(subString[2].length() == 0){
			return false;
		}
		
		if(subString[2].toUpperCase().contains("ALL")){	//delete all empty flow table
			List<OFFlowMod> flowEntryList = pofManager.iGetAllFlowEntry(switchId, globalTableId);
			if(flowEntryList != null){
				for(OFFlowMod flowEntry : flowEntryList){
					pofManager.iDelFlowEntry(switchId, globalTableId, flowEntry.getIndex());
				}
			}
			
			printlnErr("Deleted all entries. \nNow the tables is emtry: ");
			
			displayTable(switchId, ofFlowTable);
			
			return true;
		}
		
		if(!subString[2].matches(GUITools.RE_DEC)){
			printlnErr("Wrong entry id!");
			return false;
		}
		
		int entryID = Integer.parseInt(subString[2]);
		if(entryID == IPMService.FLOWENTRYID_INVALID){
			return false;
		}
		
		pofManager.iDelFlowEntry(switchId, globalTableId, entryID);
		
		return true;
	}


	/**
	 * set OFInstructionGotoTable with "nextTableId.packetOffset"
	 * @param ofInstructionGotoTable
	 * @param curIns
	 * @param switchId
	 * @return parameters contained ofInstructionGotoTable 
	 */
	
	private OFInstruction parseToInstruction(final OFInstructionGotoTable ofInstructionGotoTable, final String curIns, final int switchId) {
		String insString[] = curIns.split(SPLIT_DOT);
		if(insString == null || insString.length != 2){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		if(!insString[0].matches(GUITools.RE_DEC)
				|| !insString[1].matches(GUITools.RE_DEC)){
			printlnErr("wrong ins parameter value");
			return null;
		}
		
		byte nextTableId = (byte)Integer.parseInt( insString[0] );
		if(nextTableId == IPMService.FLOWTABLEID_INVALID){
			return null;
		}
		OFFlowTable flowTable = pofManager.iGetFlowTable(switchId, nextTableId);
		if(flowTable == null ){
			printlnErr("Wrong next table id");
			return null;
		}
		
		if(flowTable.getTableType() == OFTableType.OF_LINEAR_TABLE){
			printlnErr("next table should not be linear table, check nextTableId");
			return null;
		}
		
		short packetOffset = (short)Integer.parseInt( insString[1] );
		
		ofInstructionGotoTable.setNextTableId(nextTableId);
		ofInstructionGotoTable.setPacketOffset(packetOffset);
		ofInstructionGotoTable.setMatchFieldNum(flowTable.getMatchFieldNum());
		ofInstructionGotoTable.setMatchList(flowTable.getMatchFieldList());
		
		return ofInstructionGotoTable;
	}

	/**
	 * set OFInstructionGotoDirectTable with "nextTableId.tableEntryIndex.packetOffset"
	 * @param ofInstructionGotoDirectTable
	 * @param curIns
	 * @param switchId
	 * @return parameters contained ofInstructionGotoDirectTable 
	 */
	private OFInstruction parseToInstruction(final OFInstructionGotoDirectTable ofInstructionGotoDirectTable, final String curIns, final int switchId) {
		String insString[] = curIns.split(SPLIT_DOT);
		if(insString == null || insString.length != 3){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		if(!insString[0].matches(GUITools.RE_DEC)
				|| !insString[1].matches(GUITools.RE_DEC)
				|| !insString[2].matches(GUITools.RE_DEC)){
			printlnErr("wrong ins parameter value");
			return null;
		}
		
		byte nextTableId = (byte)Integer.parseInt( insString[0] );
		if(nextTableId == IPMService.FLOWTABLEID_INVALID){
			return null;
		}
		OFFlowTable flowTable = pofManager.iGetFlowTable(switchId, nextTableId);
		if(flowTable == null){
			printlnErr("Wrong next table id");
			return null;
		}
		if(flowTable.getTableType() != OFTableType.OF_LINEAR_TABLE){
			printlnErr("next table should be a linear table, check nextTableId");
			return null;
		}
		
		int tableEntryIndex = Integer.parseInt( insString[1] );
		if(tableEntryIndex == IPMService.FLOWENTRYID_INVALID
				|| null == pofManager.iGetFlowEntry(switchId, nextTableId, tableEntryIndex)){
			printlnErr("Wrong entry id");
			return null;
		}
		
		byte packetOffset = (byte)Integer.parseInt( insString[2] );
		
		ofInstructionGotoDirectTable.setNextTableId(nextTableId);
		ofInstructionGotoDirectTable.setTableEntryIndex(tableEntryIndex);
		ofInstructionGotoDirectTable.setPacketOffset(packetOffset);
		
		return ofInstructionGotoDirectTable;
	}
	
	/**
	 * set ofInstructionMeter with "meterId"
	 * @param ofInstructionMeter
	 * @param curIns
	 * @param switchId
	 * @return parameters contained ofInstructionMeter 
	 */
	private OFInstruction parseToInstruction(final OFInstructionMeter ofInstructionMeter, final String curIns, final int switchId) {
		String insString[] = curIns.split(SPLIT_DOT);
		if(insString == null || insString.length != 1){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		if(!insString[0].matches(GUITools.RE_DEC)){
			printlnErr("wrong ins parameter value");
			return null;
		}
		
		int meterId = (byte)Integer.parseInt( insString[0] );
		if(meterId == IPMService.METER_INVALID){
			return null;
		}
		if(pofManager.iGetMeter(switchId, meterId) == null ){
			printlnErr("Wrong meter id");
			return null;
		}
		
		ofInstructionMeter.setMeterId(meterId);
		
		return ofInstructionMeter;
	}

	/**
	 * set ofInstructionWriteMetadata with "metadataName.value"
	 * @param ofInstructionWriteMetadata
	 * @param curIns
	 * @return parameters contained ofInstructionWriteMetadata 
	 */
	private OFInstruction parseToInstruction(final OFInstructionWriteMetadata ofInstructionWriteMetadata, final String curIns) {
		String insString[] = curIns.split(SPLIT_DOT);
		if(insString == null || insString.length != 2){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		//value
		int value;
		if(insString[1].matches(GUITools.RE_PMDEC)){
			value = (int)Long.parseLong( insString[1] );
		}else if(insString[1].matches(GUITools.RE_0xHEX)){
			value = (int)Long.parseLong( insString[1].substring(2), 16 );
		}else{
			printlnErr("wrong ins parameter value");
			return null;
		}
		
		//check metadata name
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		OFMatch20 metadata = null;
		for(OFMatch20 metadataField : metadataList){
			if(metadataField.getFieldName().equalsIgnoreCase(insString[0])){
				metadata = metadataField;
				
				break;
			}
		}
		
		if(metadata == null){
			printlnErr("metadata[name= " + insString[0] + "] does not exist");
			return null;
		}
		
		ofInstructionWriteMetadata.setMetadataOffset(metadata.getOffset());
		ofInstructionWriteMetadata.setWriteLength(metadata.getLength());
		ofInstructionWriteMetadata.setValue(value);
		
		return ofInstructionWriteMetadata;
	}

	/**
	 * set ofInstructionWriteMetadataFromPacket with "metadataName.packetOffset"
	 * @param ofInstructionWriteMetadataFromPacket
	 * @param curIns
	 * @return parameters contained ofInstructionWriteMetadataFromPacket 
	 */
	private OFInstruction parseToInstruction(final OFInstructionWriteMetadataFromPacket ofInstructionWriteMetadataFromPacket, final String curIns) {
		String insString[] = curIns.split(SPLIT_DOT);
		if(insString == null || insString.length != 2){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		if(!insString[1].matches(GUITools.RE_DEC)){
			printlnErr("wrong ins parameter value");
			return null;
		}
		
		//check metadata name
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		OFMatch20 metadata = null;
		for(OFMatch20 metadataField : metadataList){
			if(metadataField.getFieldName().equalsIgnoreCase(insString[0])){
				metadata = metadataField;
				
				break;
			}
		}
		
		if(metadata == null){
			printlnErr("metadata[name= " + insString[0] + "] does not exist");
			return null;
		}
		
		short packetOffset = (short)Integer.parseInt(insString[1]);
		
		ofInstructionWriteMetadataFromPacket.setMetadataOffset(metadata.getOffset());
		ofInstructionWriteMetadataFromPacket.setWriteLength(metadata.getLength());
		ofInstructionWriteMetadataFromPacket.setPacketOffset(packetOffset);
		
		return ofInstructionWriteMetadataFromPacket;
	}

	/**
	 * set ofInstructionApplyActions with format<br>
	 * 		"OFActionType1.para1.para2.para3$OFActionType2.para1.para2.para3)" <br>
	 * Use "$" to separate actions;
	 * 
	 * @param ofInstructionApplyActions
	 * @param curIns
	 * @return actions contained ofInstructionApplyActions 
	 */
	private OFInstruction parseToInstruction(final OFInstructionApplyActions ofInstructionApplyActions, final String curIns, final int switchId) {
		//split actions
		String actionString[] = curIns.split(SPLIT_DOLLAR);
		if(actionString == null || actionString.length == 0){
			printlnErr("wrong ins parameter number");
			return null;
		}
		
		List<OFAction> actionList = new ArrayList<OFAction>();
		
		for(String curAction : actionString){
			OFAction action = parseToAction(curAction, switchId);
			if(action != null){
				actionList.add(action);
			}else{
				return null;
			}
		}
		if(actionList.size() == 0){
			printlnErr("No action");
			return null;
		}
		if(CHECK_GLOBAL){
			if(actionList.size() > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION){
				printlnErr("action number [= " + actionList.size() + "] > MAX_NUM [= " +  OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION + "]");
				return null;
			}
		}
		ofInstructionApplyActions.setActionList(actionList);
		ofInstructionApplyActions.setActionNum((byte) actionList.size());
		
		return ofInstructionApplyActions;
	}
	
	private OFAction parseToAction(final String actionString, final int switchId){
		String curAction = actionString;
		for(OFActionType actionType : ACTIONS){
			if(curAction.toUpperCase().startsWith(actionType.name())){
				OFAction action = null;
				
				//use startsWith make SET_FIELD_FROM_METADATA match SET_FIELD, 
				//so here do a special processing
				if(curAction.toUpperCase().contains(OFActionType.SET_FIELD_FROM_METADATA.name())){
					actionType = OFActionType.SET_FIELD_FROM_METADATA;
				}else if(curAction.toUpperCase().contains(OFActionType.SET_FIELD.name())){
					actionType = OFActionType.SET_FIELD;
				}
				
				curAction = curAction.substring(actionType.name().length());
				if(!curAction.isEmpty()){
					curAction = curAction.substring(1);
				}
				
				switch(actionType){
					case OUTPUT:
						action = parseToAction(new OFActionOutput(), curAction, switchId);
						break;
					case SET_FIELD:
						action = parseToAction(new OFActionSetField(), curAction);
						break;
					case SET_FIELD_FROM_METADATA:
						action = parseToAction(new OFActionSetFieldFromMetadata(), curAction);
						break;
					case MODIFY_FIELD:
						action = parseToAction(new OFActionModifyField(), curAction);
						break;
					case ADD_FIELD:
						action = parseToAction(new OFActionAddField(), curAction);
						break;
					case DELETE_FIELD:
						action = parseToAction(new OFActionDeleteField(), curAction);
						break;
					case CALCULATE_CHECKSUM:
						action = parseToAction(new OFActionCalculateCheckSum(), curAction);
						break;
					case GROUP:
						action = parseToAction(new OFActionGroup(), curAction, switchId);
						break;
					case DROP:
						action = parseToAction(new OFActionDrop(), curAction);
						break;
					case PACKET_IN:
						action = parseToAction(new OFActionPacketIn(), curAction);
						break;
					case COUNTER:
						action = parseToAction(new OFActionCounter(), curAction, switchId);
						break;
					default:
						return null;
				}

				if(action == null){
					return null;
				}
				
				return action;
			}
		}
		printlnErr("Wrong action type");
		return null;
	}

	/**
	 * set ofActionOutput with "portId.metadataOffset.metadataLength.packetOffset"
	 * @param ofActionOutput
	 * @param curAction
	 * @param switchId
	 * @return parameters contained ofActionOutput 
	 */
	private OFAction parseToAction(final OFActionOutput ofActionOutput, final String curAction, final int switchId) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 4){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)
				|| !actionString[1].matches(GUITools.RE_DEC)
				|| !actionString[2].matches(GUITools.RE_DEC)
				|| !actionString[3].matches(GUITools.RE_DEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		int portId = Integer.parseInt(actionString[0]);
		short metadataOffset = (short)Integer.parseInt(actionString[1]);
		short metadataLength = (short)Integer.parseInt(actionString[2]);
		short packetOffset = (short)Integer.parseInt(actionString[3]);
		
		//check portId
		if( !pofManager.iGetAllPortId(switchId).contains(portId) ){
			printlnErr("Wrong port id [=" + portId + "]!");
			return null;
		}
		
		//check metadata offset/length
		if(metadataOffset < 0){
			printlnErr("Wrong metadata offset");
			return null;
		}
		if(metadataLength < 0){
			printlnErr("Wrong metadata length");
			return null;
		}
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		if(0 == metadataList.size() ){
			if(metadataOffset != 0){
				printlnErr("Wrong metadataOffset [= " + metadataOffset + "]!");
				return null;
			}
			
			if(metadataLength != 0){
				printlnErr("Wrong metadataLength [= " + metadataLength + "]!");
				return null;
			}
		}else{
			OFMatch20 lastMetadata = metadataList.get(metadataList.size() - 1);
			int mdMaxLength = lastMetadata.getOffset() + lastMetadata.getLength();
			
			if(metadataOffset > mdMaxLength){
				printlnErr("Wrong metadataOffset [" + metadataOffset + " > mdMaxLength(" + mdMaxLength + ")]!");
				return null;
			}
			
			if(metadataOffset + metadataLength > mdMaxLength){
				printlnErr("metadataOffset + metadataLength [" + metadataOffset + metadataLength + " > mdMaxLength(" + mdMaxLength + ")]!");
				return null;
			}
		}
		
		ofActionOutput.setPortId(portId);
		ofActionOutput.setMetadataOffset(metadataOffset);
		ofActionOutput.setMetadataLength(metadataLength);
		ofActionOutput.setPacketOffset(packetOffset);
		
		return ofActionOutput;
	}

	/**
	 * set ofActionSetField with "fid.hex_value.hex_mask"
	 * @param ofActionSetField
	 * @param curAction
	 * @return parameters contained ofActionSetField 
	 */
	private OFAction parseToAction(final OFActionSetField ofActionSetField, final String curAction) {
		OFMatchX matchX = parseToMatchX(curAction);
		if(matchX == null){
			return null;
		}
		
		if(matchX.getFieldId() == OFMatch20.METADATA_FIELD_ID){
			printlnErr("Wrong field id");
			return null;
		}
		
		ofActionSetField.setFieldSetting(matchX);
		
		return ofActionSetField;
	}

	/**
	 * set ofActionSetFieldFromMetadata with "fid.metadataOffset"
	 * @param ofActionSetFieldFromMetadata
	 * @param curAction
	 * @return parameters contained ofActionSetFieldFromMetadata 
	 */
	private OFAction parseToAction(final OFActionSetFieldFromMetadata ofActionSetFieldFromMetadata, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 2){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)
				|| !actionString[1].matches(GUITools.RE_DEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		short fieldId = (short)Integer.parseInt(actionString[0]);
		short metadataOffset = (short)Integer.parseInt(actionString[1]);
		
		if(fieldId == -1 || fieldId == IPMService.FIELDID_INVALID){
			printlnErr("Wrong field id");
			return null;
		}
		
		OFMatch20 field = pofManager.iGetMatchField(fieldId);
		
		if(field == null){
			printlnErr("fieldId[= " + fieldId + "] does not exist");
			return null;
		}
		
	
		//check metadata offset
		if(metadataOffset < 0){
			return null;
		}
		List<OFMatch20> metadataList = pofManager.iGetMetadata();
		if(0 == metadataList.size() ){
			if(metadataOffset != 0){
				printlnErr("metadata list is empty");
				return null;
			}
		}else{
			OFMatch20 lastMetadata = metadataList.get(metadataList.size() - 1);
			int mdMaxLength = lastMetadata.getOffset() + lastMetadata.getLength();
			
			if(metadataOffset > mdMaxLength){
				printlnErr("metadataOffset[=" + metadataOffset + "] is bigger than metadata length[=" + mdMaxLength + "]");
				return null;
			}
		}
		
		try {
			ofActionSetFieldFromMetadata.setFieldSetting(field.clone());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ofActionSetFieldFromMetadata.setMetadataOffset(metadataOffset);
		
		return ofActionSetFieldFromMetadata;
	}

	/**
	 * set ofActionModifyField with "fid.increment"
	 * @param ofActionModifyField
	 * @param curAction
	 * @return parameters contained ofActionModifyField 
	 */
	private OFAction parseToAction(final OFActionModifyField ofActionModifyField, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 2){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)
				|| !actionString[1].matches(GUITools.RE_PMDEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		short fieldId = (short)Integer.parseInt(actionString[0]);
		int increment = (int)Integer.parseInt(actionString[1]);
		
		if(fieldId == -1 || fieldId == IPMService.FIELDID_INVALID){
			printlnErr("Wrong field id");
			return null;
		}
		
		OFMatch20 field = pofManager.iGetMatchField(fieldId);
		
		if(field == null){
			printlnErr("fieldId[= " + fieldId + "] does not exist");
			return null;
		}
		
		try {
			ofActionModifyField.setMatchField(field.clone());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ofActionModifyField.setIncrement(increment);
		
		return ofActionModifyField;
	}

	/**
	 * set ofActionAddField with "newFieldName.newFieldPosition.newFieldLength.fieldValue" (if the added field is a new OFMatch20) <br>
	 * 		or "existedFieldId.fieldValue" (if the added field is a existed OFMatch20)
	 * @param ofActionAddField
	 * @param curAction
	 * @return parameters contained ofActionAddField 
	 */
	private OFAction parseToAction(final OFActionAddField ofActionAddField, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null){
			return null;
		}
		
		short fieldId;
		short fieldPosition;
		short fieldLength;
		long fieldValue;
		
		if(actionString.length == 4){	//the added field is a new OFMatch20
			//"newFieldName.newFieldPosition.newFieldLength.fieldValue"
			if(actionString[0].length() == 0
					|| !actionString[1].matches(GUITools.RE_DEC)
					|| !actionString[2].matches(GUITools.RE_DEC)){
				printlnErr("wrong action parameter value");
				return null;
			}
			
			if(actionString[3].matches(GUITools.RE_0xHEX)){
				fieldValue = new BigInteger(actionString[3].substring(2), 16).longValue();
			}else if(actionString[3].matches(GUITools.RE_PMDEC)){
				fieldValue = new BigInteger(actionString[3]).longValue();
			}else{
				printlnErr("wrong field value");
				return null;
			}
			
			String fieldName = actionString[0];			
			fieldPosition = (short)Integer.parseInt(actionString[1]);
			fieldLength = (short)Integer.parseInt(actionString[2]);
			
			fieldId = pofManager.iNewField(fieldName, fieldLength, fieldPosition);
			if(fieldId == IPMService.FIELDID_INVALID){
				printlnErr("create new field failed");
				return null;
			}
			
		}else if(actionString.length == 2){	//the added field is a existed OFMatch20.
			//"existedFieldId.fieldValue"
			if(!actionString[0].matches(GUITools.RE_DEC)){
				printlnErr("wrong action parameter value");
				return null;
			}
			
			if(actionString[1].matches(GUITools.RE_0xHEX)){
				fieldValue = new BigInteger(actionString[1].substring(2), 16).longValue();
			}else if(actionString[1].matches(GUITools.RE_PMDEC)){
				fieldValue = new BigInteger(actionString[1]).longValue();
			}else{
				printlnErr("wrong field value");
				return null;
			}
			
			fieldId = (short)Integer.parseInt(actionString[0]);
			if(fieldId == IPMService.FIELDID_INVALID || fieldId == OFMatch20.METADATA_FIELD_ID){
				printlnErr("wrong field id");
				return null;
			}
			
			OFMatch20 matchField = pofManager.iGetMatchField(fieldId);
			
			if(matchField == null){
				printlnErr("fieldId[= " + fieldId + "] does not exist");
				return null;
			}
			
			fieldPosition = matchField.getOffset();
			fieldLength = matchField.getLength();
		}else{
			printlnErr("wrong action parameter number");
			return null;
		}
		
		ofActionAddField.setFieldId(fieldId);
		ofActionAddField.setFieldPosition(fieldPosition);
		ofActionAddField.setFieldLength(fieldLength);
		ofActionAddField.setFieldValue(fieldValue);
		
		return ofActionAddField;
	}

	/**
	 * set ofActionDeleteField with "fieldPosition.fieldLength"
	 * @param ofActionDeleteField
	 * @param curAction
	 * @return parameters contained ofActionDeleteField 
	 */
	private OFAction parseToAction(final OFActionDeleteField ofActionDeleteField, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 2){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)
				|| !actionString[1].matches(GUITools.RE_DEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		short fieldPosition = (short)Integer.parseInt(actionString[0]);
		int fieldLength = Integer.parseInt(actionString[1]);
		
		ofActionDeleteField.setFieldPosition(fieldPosition);
		ofActionDeleteField.setFieldLength(fieldLength);
		
		return ofActionDeleteField;
	}
	
	/**
	 * set ofActionCalculateCheckSum with "checksumPosition.checksumLength.calcStartPosition.calcLength"
	 * @param ofActionCalculateCheckSum
	 * @param curAction
	 * @return parameters contained ofActionCalculateCheckSum 
	 */
	private OFAction parseToAction(final OFActionCalculateCheckSum ofActionCalculateCheckSum, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 4){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)
				|| !actionString[1].matches(GUITools.RE_DEC)
				|| !actionString[2].matches(GUITools.RE_DEC)
				|| !actionString[3].matches(GUITools.RE_DEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		short checksumPosition = (short)Integer.parseInt(actionString[0]);
		short checksumLength = (short)Integer.parseInt(actionString[1]);
		short calcStartPosition = (short)Integer.parseInt(actionString[2]);
		short calcLength = (short)Integer.parseInt(actionString[3]);
		
		ofActionCalculateCheckSum.setChecksumPosition(checksumPosition);
		ofActionCalculateCheckSum.setChecksumLength(checksumLength);
		ofActionCalculateCheckSum.setCalcStartPosition(calcStartPosition);
		ofActionCalculateCheckSum.setCalcLength(calcLength);
		
		return ofActionCalculateCheckSum;
	}

	/**
	 * set ofActionGroup with "groupId"
	 * @param ofActionGroup
	 * @param curAction
	 * @param switchId
	 * @return parameters contained ofActionGroup 
	 */
	private OFAction parseToAction(final OFActionGroup ofActionGroup, final String curAction, final int switchId) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 1){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		if(!actionString[0].matches(GUITools.RE_DEC)){
			printlnErr("wrong action parameter value");
			return null;
		}
		
		int groupId = Integer.parseInt(actionString[0]);
		if(groupId == IPMService.GROUPID_INVALID
				|| null == pofManager.iGetGroupEntry(switchId, groupId)){
			printlnErr("wrong group id");
			return null;
		}
		
		ofActionGroup.setGroupId(groupId);
		
		return ofActionGroup;
	}

	/**
	 * set ofActionDrop with "reason", reason should be the string of {@link OFDropReason#OFDropReason}, e.g. "TIMEOUT"
	 * @param ofActionDrop
	 * @param curAction
	 * @return parameters contained ofActionDrop 
	 */
	private OFAction parseToAction(final OFActionDrop ofActionDrop, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 1){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		OFDropReason reason = null;
		
		actionString[0] = actionString[0].toUpperCase().replace("OFPDT_", "");
		for(OFDropReason dropReason : OFDropReason.values()){
			if( dropReason.name().contains( actionString[0] ) ){
				reason = dropReason;
				break;
			}
		}
		
		if(reason == null){
			printlnErr("wrong dropReason");
			return null;
		}
		
		ofActionDrop.setReason(reason.ordinal());
		
		return ofActionDrop;
	}

	/**
	 * set ofActionPacketIn with "reason", reason should be the string of {@link OFActionPacketIn#OFPacketInReason}, e.g. "HIT_MISS"
	 * @param ofActionPacketIn
	 * @param curAction
	 * @return parameters contained ofActionPacketIn 
	 */
	private OFAction parseToAction(final OFActionPacketIn ofActionPacketIn, final String curAction) {
		String actionString[] = curAction.split(SPLIT_DOT);
		if(actionString == null || actionString.length != 1){
			printlnErr("wrong action parameter number");
			return null;
		}
		
		OFPacketInReason reason = null;
		
		actionString[0] = actionString[0].toUpperCase().replace("OFPR_", "");
		for(OFPacketInReason packetInReason : OFPacketInReason.values()){
			if( packetInReason.name().contains( actionString[0] ) ){
				reason = packetInReason;
				break;
			}
		}
		
		if(reason == null){
			printlnErr("wrong packetInReason");
			return null;
		}
		
		ofActionPacketIn.setReason(reason.ordinal());
		
		return ofActionPacketIn;
	}

	/**
	 * set ofActionCounter with a new counterId automatically
	 * @param ofActionCounter
	 * @param curAction
	 * @return parameters contained ofActionPacketIn 
	 */
	private OFAction parseToAction(final OFActionCounter ofActionCounter, final String curAction, final int switchId) {
		if(ofActionCounter.getCounterId() == IPMService.COUNTERID_INVALID){
			int counterId = pofManager.iAllocateCounter(switchId);
			ofActionCounter.setCounterId(counterId);
		}
		return ofActionCounter;
	}
	
	/**
	 * display group entry <br>
	 * <p>
	 * format: <br>
	 * 		DISPLAY GROUP [SwitchId|ALL][.GroupID|.ALL]
	 * <p>
	 * e.g.:<br>
	 * 		DISPLAY GROUP					//display all switches' all group entries <br>
	 * 		DISPLAY GROUP ALL				//display all switches' all group entries <br>
	 * <p>
	 * 		DISPLAY GROUP 11223344			//display the switch(id=11223344)'s all group entries <br>
	 * 		DISPLAY GROUP 11223344.ALL		//display the switch(id=11223344)'s all group entries <br>
	 * <p>
	 * 		DISPLAY GROUP 11223344.3		//display the switch(id=11223344)'s group entry(id=3) <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayGroup(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){//display all switches' all tables' all groups
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayGroup(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.groupID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayGroup(switchId);
			}
			return true;
		}
		
		//subString[0] is switchID
		if(subString[0] == null || subString[0].length() == 0){
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//display all switches' all tables' all groups
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayGroup(switchId);
			}
			return true;
		}
		
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		if(subString.length <= 1){	//if only have switch id, display a switch's all groups
			displayGroup(switchId);
			return true;
		}
		
		//subString[1] is groupID
		if(subString[1] == null || subString[1].length() == 0 || subString[1].toUpperCase().contains("ALL")){
			//if only have switch id, display a switch's all group entries
			displayGroup(switchId);
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong group id!");
			return false;
		}

		int groupId = Integer.parseInt(subString[1]);
		if(groupId == IPMService.GROUPID_INVALID){
			printlnErr("Wrong group id!");
			return false;
		}
		
		displayGroup(switchId, groupId);
		
		return true;
	}
	
	/**
	 * display a switch's all group entries' information
	 * @param switchId
	 */
	private void displayGroup(final int switchId){
		List<OFGroupMod> groupList = pofManager.iGetAllGroups(switchId);
		if(groupList != null && groupList.size() != 0){
			println("Switch [id= " + switchId + "]: ");
			for(OFGroupMod ofGroupEntry : groupList){
				println("  GroupEntry [gid= " + ofGroupEntry.getGroupId() + "]: " + ofGroupEntry.toString() );
			}
		}
	}
	
	/**
	 * display a group entry's information
	 * @param switchId
	 */
	private void displayGroup(final int switchId, final int groupId){
		OFGroupMod ofGroupEntry = pofManager.iGetGroupEntry(switchId, groupId);
		
		if(ofGroupEntry != null){
			println("  GroupEntry [gid= " + ofGroupEntry.getGroupId() + "]: " + ofGroupEntry.toString() );
		}else{
			printlnErr("Group[id= " + groupId + "] does not exist.");
		}
	}
	
	/**
	 * add a new group. <p>
	 * format: <br>
	 *  	ADD  GROUP  switchid  groupType OFActionType1.para1.para2.para3 OFActionType2.para1.para2.para3
	 * <p>
	 * e.g.:<br>
	 *  	ADD GROUP 11223344 ALL SET_FIELD.fid.hex_v.hex_m  CALCULATE_CHECKSUM.1.2.3.4  OUTPUT.5.0.0.0 <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addGroup(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 2){
			return false;
		}
		
		int switchId;
		int currentCmdIndex = curCmdListIndex;
		int groupId;
		List<OFAction> actionList = new ArrayList<OFAction>();
		
		//1. switchid
		String currentCmd = cmdList.get(currentCmdIndex);
		if(currentCmd == null 
				|| currentCmd.length() == 0 
				|| !currentCmd.matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(currentCmd);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		
		//2. groupType
		currentCmdIndex++;
		
		OFGroupType type = null;
		currentCmd = cmdList.get(currentCmdIndex);
		if(currentCmd == null){
			return false;
		}
		currentCmd = currentCmd.toUpperCase().replace("OFPGT_", "");
		for(OFGroupType groupType : OFGroupType.values()){
			if( groupType.name().contains( currentCmd ) ){
				type = groupType;
				break;
			}
		}
		
		if(type == null){
			printlnErr("Wrong groupType");
			return false;
		}
		
		
		//3. actions
		currentCmdIndex++;
		int cmdNum = cmdList.size();
		for( ; currentCmdIndex < cmdNum; currentCmdIndex++ ){
			currentCmd = cmdList.get(currentCmdIndex);
			
			OFAction action = parseToAction(currentCmd, switchId);
			if(action != null){
				actionList.add(action);
			}else{
				return false;
			}
		}
		
		if(actionList.size() == 0){
			printlnErr("No action");
			return false;
		}
		if(CHECK_GLOBAL){
			if(actionList.size() > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP){
				printlnErr("action number [= " + actionList.size() + "] > MAX_NUM [= " +  OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP + "]");
				return false;
			}
		}
		
		groupId = pofManager.iAddGroupEntry(switchId, 
											(byte)type.ordinal(), 
											(byte)actionList.size(), 
											actionList);
		if(groupId == IPMService.GROUPID_INVALID){
			printlnErr("create group failed");
			return false;
		}
		
		println("Created new group entry!");
		this.displayGroup(switchId, groupId);
		
		return true;
	}

	/**
	 * modify a existed group. <p>
	 * format: <br>
	 *  	MODIFY  GROUP  switchid.groupId  groupType OFActionType1.para1.para2.para3 OFActionType2.para1.para2.para3
	 * <p>
	 * e.g.:<br>
	 *  	MODIFY GROUP 11223344.3 ALL SET_FIELD.fid.hex_v.hex_m  CALCULATE_CHECKSUM.1.2.3.4  OUTPUT.5.0.0.0 <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyGroup(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 2){
			return false;
		}
		
		int switchId;
		int currentCmdIndex = curCmdListIndex;
		int groupId;
		List<OFAction> actionList = new ArrayList<OFAction>();
		
		//1. switchid.groupId
		String currentCmd = cmdList.get(currentCmdIndex);
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null 
				|| subString[0].length() == 0
				|| !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is groupId
		if(subString[1] == null 
				|| subString[1].length() == 0
				|| !subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong group id!");
			return false;
		}
		groupId = Integer.parseInt(subString[1]);
		if(groupId == IPMService.GROUPID_INVALID){
			printlnErr("Wrong group id!");
			return false;
		}
		OFGroupMod ofGroupMod = pofManager.iGetGroupEntry(switchId, groupId);

		if(ofGroupMod == null){
			printlnErr("group [id= " + groupId + " does NOT exist!");
			return false;
		}
		
		//2. groupType
		currentCmdIndex++;
		
		OFGroupType type = null;
		currentCmd = cmdList.get(currentCmdIndex);
		if(currentCmd == null){
			return false;
		}
		currentCmd = currentCmd.toUpperCase().replace("OFPGT_", "");
		for(OFGroupType groupType : OFGroupType.values()){
			if( groupType.name().contains( currentCmd ) ){
				type = groupType;
				break;
			}
		}
		
		if(type == null){
			printlnErr("Wrong groupType");
			return false;
		}
		
		
		//3. actions
		currentCmdIndex++;
		int cmdNum = cmdList.size();
		for( ; currentCmdIndex < cmdNum; currentCmdIndex++ ){
			currentCmd = cmdList.get(currentCmdIndex);
			
			OFAction action = parseToAction(currentCmd, switchId);
			if(action != null){
				actionList.add(action);
			}else{
				return false;
			}
		}
		
		if(actionList.size() == 0){
			printlnErr("No action");
			return false;
		}
		
		if(CHECK_GLOBAL){
			if(actionList.size() > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP){
				printlnErr("action number [= " + actionList.size() + "] > MAX_NUM [= " +  OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP + "]");
				return false;
			}
		}
		
		
		boolean succ = pofManager.iModifyGroupEntry(switchId, 
													groupId, 
													(byte)type.ordinal(), 
													(byte)actionList.size(), 
													actionList);
		if(succ == false){
			println("Modify group failed, please check the parameters");
	    	return false;
		}
		
		println("Successfully modified the group!");
		this.displayGroup(switchId, groupId);
		
		return true;
	}
	
	/**
	 * delete a group
	 * <p>
	 * format: <br>
	 * 		DELETE GROUP SwitchId.[GroupId|ALL]
	 * <p>
	 * e.g.: <br>
	 * 		DELETE GROUP 11223344.ALL	//delete the switch(id=11223344)'s all groups. <br>
	 * 		DELETE GROUP 11223344.3		//delete the switch(id=11223344)'s one group(id=3). <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean delGroup(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.groupID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0].length() == 0 || !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}

			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is groupId
		if(subString[1].length() == 0){
			return false;
		}
		
		if(subString[1].toUpperCase().contains("ALL")){	//delete all groups
			List<OFGroupMod> groupList = pofManager.iGetAllGroups(switchId);
			if(groupList != null){
				for(OFGroupMod group : groupList){
					pofManager.iFreeGroupEntry(switchId, group.getGroupId());
				}
			}
			
			printlnErr("Deleted all groups. ");
			
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong group id!");
			return false;
		}
		
		int groupID = Integer.parseInt(subString[1]);
		if(groupID == IPMService.GROUPID_INVALID){
			printlnErr("Wrong group id!");
			return false;
		}
		
		pofManager.iFreeGroupEntry(switchId, groupID);
		
		return true;
	}

	/**
	 * display group entry <br>
	 * <p>
	 * format: <br>
	 * 		DISPLAY METER [SwitchId|ALL][.MeterID|.ALL]
	 * <p>
	 * e.g.:<br>
	 * 		DISPLAY METER					//display all switches' all meters <br>
	 * 		DISPLAY METER ALL				//display all switches' all meters <br>
	 * <p>
	 * 		DISPLAY METER 11223344			//display the switch(id=11223344)'s all meters <br>
	 * 		DISPLAY METER 11223344.ALL		//display the switch(id=11223344)'s all meters <br>
	 * <p>
	 * 		DISPLAY METER 11223344.5		//display the switch(id=11223344)'s meters(id=5) <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayMeter(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){//display all switches' all tables' all meters
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayMeter(switchId);
			}
			return true;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.meterID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length == 0){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null || subString[0].length() == 0){
			return false;
		}
		
		if(subString[0].toUpperCase().contains("ALL")){	//display all switches' all tables' all meters
			List<Integer> allSwitchID = pofManager.iGetAllSwitchID();
			for(Integer switchId : allSwitchID){
				displayMeter(switchId);
			}
			return true;
		}
		
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		if(subString.length <= 1){	//if only have switch id, display a switch's all meters
			displayMeter(switchId);
			return true;
		}
		
		//subString[1] is metersID
		if(subString[1] == null || subString[1].length() == 0 || subString[1].toUpperCase().contains("ALL")){
			//if only have switch id, display a switch's all meters
			displayMeter(switchId);
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong meter id!");
			return false;
		}

		int meterId = Integer.parseInt(subString[1]);
		if(meterId == IPMService.METER_INVALID){
			printlnErr("Wrong meter id!");
			return false;
		}
		
		displayMeter(switchId, meterId);
		
		return true;
	}
	
	/**
	 * display a switch's all meters' information
	 * @param switchId
	 */
	private void displayMeter(final int switchId){
		List<OFMeterMod> meterList = pofManager.iGetAllMeters(switchId);
		if(meterList != null && meterList.size() != 0){
			println("Switch [id= " + switchId + "]: ");
			for(OFMeterMod meter : meterList){
				println("  Meter [mid= " + meter.getMeterId() + "]: " + meter.toString() );
			}
		}
	}
	
	/**
	 * display a group entry's information
	 * @param switchId
	 */
	private void displayMeter(final int switchId, final int meterId){
		OFMeterMod meter = pofManager.iGetMeter(switchId, meterId);
		
		if(meter != null){
			println("  Meter [mid= " + meter.getMeterId() + "]: " + meter.toString() );
		}else{
			printlnErr("Meter[id= " + meterId + "] does not exist.");
		}
	}
	
	/**
	 * add a new meter. <p>
	 * format: <br>
	 *  	ADD  METER  switchid  rate		//rate is kbps
	 * <p>
	 * e.g.:<br>
	 *  	ADD METER 11223344 32<br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean addMeter(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() != 4){
			return false;
		}
		
		int switchId;
		short rate;
		int meterId;
		
		//1. switchid
		String currentCmd = cmdList.get(curCmdListIndex);
		if(currentCmd == null 
				|| currentCmd.length() == 0 
				|| !currentCmd.matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(currentCmd);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//2. rate
		currentCmd = cmdList.get(curCmdListIndex + 1);
		if(currentCmd == null 
				|| currentCmd.length() == 0 
				|| !currentCmd.matches(GUITools.RE_DEC)){
			printlnErr("Wrong rate value");
			return false;
		}
		
		rate = (short)Integer.parseInt(currentCmd);
		
		meterId = pofManager.iAddMeterEntry(switchId, rate);
		if(meterId == IPMService.METER_INVALID){
			printlnErr("create meter failed");
			return false;
		}
		
		println("Created new meter!");
		this.displayMeter(switchId, meterId);
		
		return true;
	}

	/**
	 * modify a existed meter. <p>
	 * format: <br>
	 *  	MODIFY METER switchid.meterId  rate		//rate is kbps
	 * <p>
	 * e.g.:<br>
	 *  	MODIFY METER 11223344.5 32<br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean modifyMeter(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex + 1){
			return false;
		}
		
		int switchId;
		int currentCmdIndex = curCmdListIndex;
		int meterId;
		short rate;
		
		//1. switchid.groupId
		String currentCmd = cmdList.get(currentCmdIndex);
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switchID
		if(subString[0] == null 
				|| subString[0].length() == 0
				|| !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is meterId
		if(subString[1] == null 
				|| subString[1].length() == 0
				|| !subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong meter id!");
			return false;
		}
		meterId = Integer.parseInt(subString[1]);
		if(meterId == IPMService.METER_INVALID){
			printlnErr("Wrong meter id!");
			return false;
		}
		
		OFMeterMod ofMeterMod = pofManager.iGetMeter(switchId, meterId);

		if(ofMeterMod == null){
			printlnErr("meter [id= " + meterId + " does NOT exist!");
			return false;
		}
		
		//2. rate
		currentCmd = cmdList.get(curCmdListIndex + 1);
		if(currentCmd == null 
				|| currentCmd.length() == 0 
				|| !currentCmd.matches(GUITools.RE_DEC)){
			printlnErr("Wrong rate value!");
			return false;
		}
		
		
		rate = (short)Integer.parseInt(currentCmd);
		if( false == pofManager.iModifyMeter(switchId, meterId, rate) ){
			printlnErr("modify meter [id= " + meterId +"] failed");
			return false;
		}


		
		println("Modified meter!");
		this.displayMeter(switchId, meterId);
		
		return true;
	}

	/**
	 * delete a meter
	 * <p>
	 * format: <br>
	 * 		DELETE METER SwitchId.[MeterId|ALL]
	 * <p>
	 * e.g.: <br>
	 * 		DELETE METER 11223344.ALL	//delete the switch(id=11223344)'s all meters. <br>
	 * 		DELETE METER 11223344.5		//delete the switch(id=11223344)'s one meter(id=5). <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean delMeter(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.meterID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switch ID
		if(subString[0].length() == 0 || !subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}

			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is meterId
		if(subString[1].length() == 0){
			return false;
		}
		
		if(subString[1].toUpperCase().contains("ALL")){	//delete all meters
			List<OFMeterMod> meterList = pofManager.iGetAllMeters(switchId);
			if(meterList != null){
				for(OFMeterMod meter : meterList){
					pofManager.iFreeMeter(switchId, meter.getMeterId());
				}
			}
			
			printlnErr("Deleted all meters. ");
			
			return true;
		}
		
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong meter id!");
			return false;
		}
		
		int meterID = Integer.parseInt(subString[1]);
		if(meterID == IPMService.METER_INVALID){
			printlnErr("Wrong meter id!");
			return false;
		}
		
		pofManager.iFreeMeter(switchId, meterID);
		
		return true;
	}

	/**
	 * display group entry <br>
	 * <p>
	 * format: <br>
	 * 		DISPLAY COUNTER SwitchId.CounterID
	 * <p>
	 * e.g.:<br>
	 * <p>
	 * 		DISPLAY COUNTER 11223344.6		//display the switch(id=11223344)'s counter(id=6)'s value <br>
	 * 
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean displayCounterValue(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.counterID
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switchID
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}
		
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is counterID
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong counter id!");
			return false;
		}

		int counterId = Integer.parseInt(subString[1]);
		if(counterId == IPMService.COUNTERID_INVALID){
			printlnErr("Wrong counter id!");
			return false;
		}
		
		displayCounter(switchId, counterId);
		
		return true;
	}
	
	/**
	 * display a group entry's information
	 * @param switchId
	 */
	private void displayCounter(final int switchId, final int counterId){
		long counterValue = pofManager.iQueryCounterValue(switchId, counterId);
		if(counterValue != -1){
			println("  Counter value[cid= " + counterId + "] = " + counterValue);
		}else{
			printlnErr(" Wrong counter id[= " + counterId + "]");
		}
		
	}

	/**
	 * reset the counter value (set counter value = 0)
	 * <p>
	 * format: <br>
	 * 		RESET COUNTER SwitchId.CounterID
	 * <p>
	 * e.g.: <br>
	 * 		RESET COUNTER 11223344.6		//reset the switch(id=11223344)'s counter(id=5)'s value. <br>
	 * 
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean resetCounterValue(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String currentCmd = cmdList.get(curCmdListIndex);
		
		//subString[0].subString[1]
		//switchID.counterId
		String subString[] = currentCmd.split(SPLIT_DOT);
		if(subString == null || subString.length != 2){
			return false;
		}
		
		//subString[0] is switch ID
		if(!subString[0].matches(SWITCHID_DEC_RE)){
			printlnErr("Wrong switch id!");
			return false;
		}

			
		int switchId = (int)Long.parseLong(subString[0]);
		if(!switchInfoMap.containsKey(switchId)){
			printlnErr("switch [id= " + switchId + " is NOT connected!");
			return false;
		}
		
		//subString[1] is counterId
		if(!subString[1].matches(GUITools.RE_DEC)){
			printlnErr("Wrong counter id!");
			return false;
		}
		
		int counterId = Integer.parseInt(subString[1]);
		if(counterId == IPMService.COUNTERID_INVALID){
			printlnErr("Wrong counter id!");
			return false;
		}
		
		if(false == pofManager.iResetCounter(switchId, counterId, true)){
			printlnErr(" Wrong counter id[= " + counterId + "]");
			return false;
		}
		
		return true;
	}
	
	protected static void print(final String str){
		System.out.print(str);
//		log.info(str);
	}
	
	protected static void println(){
		System.out.println();
//		log.info("");
	}
	
	protected static void println(final String str){
		System.out.println(str);
//		log.info(str);
	}
	
	protected static void printlnErr(final String str){
		System.err.println(str);
//		log.error(str);
	}
	
	private void displayAbout() {
		println("     " + CONSOLE_TITLE);
		
		println("     #                     v" + IPMService.POF_VERSION + "                     #");
		println("     #                                                #");
		
		println("     #   For More Informatioin, Please Visit:         #");
		println("     #          http://www.poforwarding.org           #");
		println("     #                                                #");
		
		println("     #   Any Bug Report or Advice, Please Email to:   #");
		println("     #          jack.songjian@huawei.com              #");
		
		println("     #                                                #");
		println("     ##################################################");
		
	}
	
	private void displayHelp() {
		println();
		println(CMDSTRING.DISPLAY + " (or " + CMDSTRING.DIS + ")"
				+ " [Type] [parameters]"
							+ "\t\tdisplay the Type's information");
		println();
		
		
		println(CMDSTRING.ADD
				+ " [Type] [parameters]"
							+ "\t\t\t\tadd a Type item");
		println(CMDSTRING.DELETE + " (or " + CMDSTRING.DEL + ")"
				+ " [Type] [parameters]"
							+ "\t\tdelete a Type item");
		println(CMDSTRING.MODIFY
				+ " [Type] [parameters]"
							+ "\t\t\tmodify a Type item");
		println();
		
		
		println(CMDSTRING.RESET
				+ " COUNTER [parameters]"
							+ "\t\t\treset the counter value");
		println(CMDSTRING.SET
				+ " PORT [parameters]"
							+ "\t\t\t\tset the port enable/disable pof function");
		println();
		
		
		println(CMDSTRING.EXIT
				+ " "
							+ "\t\t\t\t\t\texit the pof controller");
		println(CMDSTRING.ABOUT
				+ " "
							+ "\t\t\t\t\tdisplay the pof controller version infomation");
		println(CMDSTRING.HELP + " (or ?)"
							+ "\t\t\t\t\tdisplay this help infomation");
		println();
		println();
	}
	
	/**
	 * save database into a *.db file. 
	 * <p>
	 * format: <br>
	 * 		SAVE filename.db 		//save database into a filename.db
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean save(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String fileName = cmdList.get(curCmdListIndex).toLowerCase();
		if( !fileName.endsWith(".db")){
			printlnErr("Please use   SAVE filename.db   to save into filename.db");
			return false;
		}
		
        File file = new File(fileName);
        if(true == file.exists()){
        	print(fileName + " is already existed, overwrite it? (Y/N): ");
        	
        	String inputString;
        	if(console != null){
        		inputString = console.readLine();
        	}else if(input != null){
        		inputString = input.nextLine();
        	}else{
        		return false;
        	}
        	
        	if(inputString == null || inputString.length() == 0){
        		return false;
        	}
        	
        	inputString = inputString.toLowerCase();
        	if(!inputString.equals("y") && !inputString.equals("yes")){
        		return false;
        	}
        }
        
		if (false == pofManager.saveAllDataIntoFile(fileName)) {
			printlnErr("Ooops, save failed.");
			return false;
		}	
		println("Saved all data into file: " + fileName);
		println();
		println();
		return true;
	}

	/**
	 * load database from a *.db file, or from a *.pofscript file. 
	 * <p>
	 * format: <br>
	 * 		LOAD filename.db 			yes		//load database from filename.db and write OFMessages to switches <br>
	 * 		LOAD filename.db 			no		//load database from filename.db and do NOT write OFMessages<br>
	 * 		LOAD finename.pofscript				//load console script from a finename.pofscript
	 * @param cmdList
	 * @param curCmdListIndex
	 * @return success or fail
	 */
	private boolean load(final List<String> cmdList, final int curCmdListIndex) {
		if(cmdList == null || cmdList.size() <= curCmdListIndex){
			return false;
		}
		
		String fileName = cmdList.get(curCmdListIndex).toLowerCase();
		boolean ret = true;
		
		if(fileName.endsWith(".db")){		//load database
			boolean writeFlag;
			if(cmdList.size() <= curCmdListIndex + 1){
				printlnErr("format:   LOAD filename.db  yes/no");
				return false;
			}
			
			if(cmdList.get(curCmdListIndex + 1).equals("YES")){
				writeFlag = true;
			}else if(cmdList.get(curCmdListIndex + 1).equals("NO")){
				writeFlag = false;
			}else{
				return false;
			}
			
			if(true == pofManager.loadAllDataFromFile(fileName)){
				if(writeFlag == true){
					if(false == pofManager.sendAllOFMessagesBasedOnDatabase()){
						printlnErr("Ooops, send OFMessage failed, please check log.");
						ret = false;
					}else{
						println("Successfully Load all data from " + fileName + " and Sent all OFMessages to switches.");
					}
				}else{
					println("Successfully Load all data from " + fileName);
				}
			}else{
				printlnErr("Ooops, load data failed.");
				ret = false;
			}
			
			println();
			println();
			
			pofManager.reloadUI();
			
			return ret;
		}else if(fileName.endsWith(".pofscript")){	//load scripts
	        File file = null;
	        BufferedReader br = null;
	        Boolean processRet;
	        String lineString;
	        int lineCounter = 1;

	        try{
	            file = new File(fileName);
	            if(false == file.exists()
	                    || true == file.isDirectory()
	                    || false == file.canRead()){
	                log.error("Read file {} fail.", fileName);
	                return false;
	            }
	            br = new BufferedReader(new FileReader(file));
	            
	            while( null != ( lineString = br.readLine() )){
	            	if(lineString.length() > 0 && !lineString.startsWith("#")){
						processRet = processCmd(lineString);
						if(processRet != null){
							if(processRet == true){
								CMD_COUNTER++;
							}else if(processRet == false){
								printlnErr("execute line " + lineCounter + " failed. (" + lineString + ")");
							}
						}
	            	}
	            	lineCounter++;
	            }
	            
	        }catch (Exception e) {
	            e.printStackTrace();
	            ret = false;
	        }finally{
	            try {
	            	if(null != br){
	            		br.close();
	            	}
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        
			println();
			println();
			
			pofManager.reloadUI();
			
	        return ret;
		}else{
			printlnErr("Please use   LOAD filename.db        to load database from filename.db");
			printlnErr("  Or use     LOAD filename.pofscript to load script from filename.pofscript");
			println();
			println();
			return false;
		}
	}

	public static ConsoleUI launchConsoleUI() {
		return new ConsoleUI();
	}
	
	public static void setConsoleUIMainPanel(final ConsoleUIMainPanel consoleUIMainPanel){
		ConsoleUI.consoleUIMainPanel = consoleUIMainPanel;
	}

    public void addSwitch(int switchId){
    	switchInfoMap.put(switchId, new ArrayList<OFMessage>());
    }

	public void processDeviceInfo(final int switchId, final OFMessage ofmsg){
		
	}


    public void removeSwitch(int switchId){
    	switchInfoMap.remove(switchId);
    }

    public void rollBack(int switchId, OFMessage sendedMsg){
    	
    }

	public void reloadUI() {
		displayAll();
	}

}
