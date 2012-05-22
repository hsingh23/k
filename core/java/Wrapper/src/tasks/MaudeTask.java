package tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MaudeTask extends Thread {
	// private static final String LOG_FILE = "maude.log";
	// private static final String LOGGER = "maude";
	private Logger _logger;
	private String _command;
	private String _outputFile;
	private String _errorFile;
	private String _xmlFile;
	private Process _maudeProcess;
	public int returnValue;

	public MaudeTask(String command, String outputFile, String errorFile, String xmlFile, Logger parentLogger) {
		_command = command;
		_outputFile = outputFile;
		_logger = parentLogger;
		_errorFile = errorFile;
		_xmlFile = xmlFile;
	}

	@Override
	public void run() {
		try {
			runMaude();
			runCommand();
			writeOutput();
			writeError();
			_maudeProcess.waitFor();
			returnValue = _maudeProcess.exitValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void runMaude() throws IOException {
		ProcessBuilder maude = new ProcessBuilder();
		List<String> commands = new ArrayList<String>();
		commands.add("maude");
		commands.add("-no-wrap");
		commands.add("-no-banner");
		commands.add("-xml-log=" + _xmlFile);
		maude.command(commands);
		
		Process maudeProcess = maude.start();
		_maudeProcess = maudeProcess;
	}
	private void runCommand() throws IOException {
		BufferedWriter maudeInput = new BufferedWriter(new OutputStreamWriter(_maudeProcess.getOutputStream()));
		maudeInput.write(_command + "\n");
		maudeInput.close();
	}
	private void writeOutput() throws IOException {
		// redirect out in log file
		BufferedReader maudeOutput = new BufferedReader(new InputStreamReader(_maudeProcess.getInputStream()));
		BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_outputFile)));
		
		String line;
		while ((line = maudeOutput.readLine()) != null) {
			// System.out.println(line);
			outputFile.write(line + "\n");
		}
		outputFile.close();
	}
	private void writeError() throws IOException {
		// redirect out in log file
		BufferedReader maudeOutput = new BufferedReader(new InputStreamReader(_maudeProcess.getErrorStream()));
		BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_errorFile)));
		
		String line;
		while ((line = maudeOutput.readLine()) != null) {
			// System.out.println(line);
			outputFile.write(line + "\n");
		}
		outputFile.close();
	}
}