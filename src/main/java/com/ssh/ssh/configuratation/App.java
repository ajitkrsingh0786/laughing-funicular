package com.ssh.ssh.configuratation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		System.out.println("Hello World!");
		String username = " ";
		String password = " ";
		String host = " ";
		// String host = " ";
		int port = 22;
//		String username = "demo";
//		String password = "password";
//		String host = "test.rebex.net";
//		int port = 22;
		String command = "pwd;cd Desktop;pwd;ls";

		// System.out.println(listFolderStructure(username, password, host, port,
		// command));
		runSudoCommand(username, password, host, command);
		System.out.println("Ajit");
	}

	public static String listFolderStructure(String username, String password, String host, int port, String command)
			throws Exception {
		Session session = null;
		ChannelExec channel = null;
		String response = null;

		try {
			session = new JSch().getSession(username, host, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
			ByteArrayOutputStream errorResponseStream = new ByteArrayOutputStream();
			OutputStream out = channel.getOutputStream();
			channel.setOutputStream(responseStream);
			channel.setErrStream(errorResponseStream);
			channel.connect();
			out.write((password + "\n").getBytes());
			out.flush();
			while (channel.isConnected()) {
				Thread.sleep(100);
			}
			String errorResponse = new String(errorResponseStream.toByteArray());
			response = new String(responseStream.toByteArray());
			if (!errorResponse.isEmpty()) {
				throw new Exception(errorResponse);
			}
		} finally {
			if (session != null) {
				session.disconnect();
			}
			if (channel != null) {
				channel.disconnect();
			}
		}
		return response;

	}

	public static void runSudoCommand(String user, String password, String host, String command) {

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		Session session;
		try {
			session = jsch.getSession(user, host, 22);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			System.out.println("Connected to " + host);
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("sudo -S -p '' " + command);
			channel.setInputStream(null);
			OutputStream out = channel.getOutputStream();
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			((ChannelExec) channel).setPty(true);
			channel.connect();
			out.write((password + "\n").getBytes());
			out.flush();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					System.out.println("Exit status: " + channel.getExitStatus());
					break;
				}
			}
			channel.disconnect();
			session.disconnect();
			System.out.println("DONE");
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
	}
}
