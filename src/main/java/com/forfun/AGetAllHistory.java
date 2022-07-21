package com.forfun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import com.util.FileOps;

public class AGetAllHistory {
	public static void main(String[] args) {

		float[] checkPoint = { 60.0f, 70.0f, 80.0f, 90.0f, 100.0f };
		String[] checkPointDescription = { "60%~", "70%~", "80%~", "90%~", "100%" };

		StringBuffer outputHeader = new StringBuffer();
		StringBuffer outputBuffer = new StringBuffer();
		StringBuffer additionalNoteHeaderBuffer = new StringBuffer();
		StringBuffer additionalNoteBuffer = new StringBuffer();

		String header = "序号, 基金代码, 基金名称, 最新净值日期, 最新净值, 最新回撤, 相对于历史最大回撤,阈值区间,--,成立以来最大回撤, 最大回撤净值, 最大回撤周期,--, 历史最高日期, 历史最高日净值, 备注";

		String additionalNoteHeader = "序号, 基金代码, 基金名称, 最新净值日期, 最新净值, 最新回撤, 相对于历史最大回撤,昨日阈值区间,今日阈值区间";

		outputHeader.append(header)
				.append("\n");

		additionalNoteHeaderBuffer
				// .append("\n")
				// .append("\n")
				// .append(", 阈值区间变化提醒：\n")
				.append(additionalNoteHeader)
				.append("\n");

		AGetAllHistory a = new AGetAllHistory();

		File inputFile = a.getInputFile(); // get the latest file as input, to get fundCode, last status

		try {
			InputStream inputStream = new FileInputStream(inputFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			int i = 0;
			br.readLine(); // 跳过第一行
			boolean firstWriteFlag = true;
			String outputFile = "";
			String alertFile = "";

			// 逐行读取
			while ((line = br.readLine()) != null) {
				String[] items = line.trim().split(Constant.splitter);
				if (items.length > 10) { // 如果
					String number = items[1].trim(); // 基金代码
					String name = items[2].trim(); // 基金名称
					String preCheckPointDesc = items[7].trim();
					outputBuffer = new StringBuffer();
					if (number.length() == 0 || number.equals("-")) {
						outputBuffer.append("-").append("\n"); // 跳过空白行
					} else {
						if (number.length() > 6) {
							number = number.substring(1); // excel中基金代码前有单引号，去掉单引号
						}
						while (number.length() < 6) {
							number = "0" + number; // 不够6位的前面补0
						}

						System.out.println(MessageFormat.format("{0}: {1}", i, number));
						i++;

						// 获取单只基金数据
						AGetHistory aGetHist = new AGetHistory(number, Constant.startDate, Constant.endDate);
						aGetHist.collectData();

						// 计算相对于历史最大回撤
						Float withdrawPercent = (float) Math.round(
								aGetHist.getLatestNode().withdraw / aGetHist.getLargestWithdrawNode().withdraw * 10000)
								/ 100;

						// 确定阈值区间
						String checkPointDesc = "";
						if (withdrawPercent > checkPoint[0]) {
							checkPointDesc = checkPointDescription[0];
							for (int m = 1; m < checkPoint.length; m++) {
								if (withdrawPercent > checkPoint[m]) {
									checkPointDesc = checkPointDescription[m];
								}
							}
						}

						// 阈值区间跟前一天不同的话，添加到 additionalNoteBuffer
						if (!checkPointDesc.equals(preCheckPointDesc)) {
							additionalNoteBuffer.append(String.valueOf(i))
									.append(Constant.splitter)
									.append('\'')
									.append(number.trim())
									.append(Constant.splitter)
									.append(name.trim())
									.append(Constant.splitter)
									.append(aGetHist.getLatestNode().getFSRQ())
									.append(Constant.splitter)
									.append(aGetHist.getLatestNode().getDWJZ())
									.append(Constant.splitter)
									.append("-" + aGetHist.getLatestNode().getWithdraw() + "%")
									.append(Constant.splitter)
									.append(withdrawPercent.toString() + "%")
									.append(Constant.splitter)
									.append(preCheckPointDesc)
									.append(Constant.splitter)
									.append(checkPointDesc)
									.append(Constant.splitter)
									.append("\n");
						}

						// 一行对应一只基金的数据
						String commentStr = "无历史分红";
						if (aGetHist.getFHSPflag()) {
							commentStr = "有历史分红";
						}
						outputBuffer.append(String.valueOf(i))
								.append(Constant.splitter)
								.append('\'')
								.append(number.trim())
								.append(Constant.splitter)
								.append(name.trim())
								.append(Constant.splitter)
								.append(aGetHist.getLatestNode().getFSRQ())
								.append(Constant.splitter)
								.append(aGetHist.getLatestNode().getDWJZ())
								.append(Constant.splitter)
								.append("-" + aGetHist.getLatestNode().getWithdraw() + "%")
								.append(Constant.splitter)
								.append(withdrawPercent.toString() + "%")
								.append(Constant.splitter)
								.append(checkPointDesc)
								.append(Constant.splitter)
								.append(Constant.splitter)
								.append("-" + aGetHist.getLargestWithdrawNode().getWithdraw() + "%")
								.append(Constant.splitter)
								.append(aGetHist.getLargestWithdrawNode().getPreHighDWJZ() + " -> "
										+ aGetHist.getLargestWithdrawNode().getDWJZ())
								.append(Constant.splitter)
								.append(aGetHist.getLargestWithdrawNode().getPreHighFSRQ() + " -> "
										+ aGetHist.getLargestWithdrawNode().getFSRQ())
								.append(Constant.splitter)
								.append(Constant.splitter)
								.append(aGetHist.getLargestNode().getFSRQ())
								.append(Constant.splitter)
								.append(aGetHist.getLargestNode().getDWJZ())
								.append(Constant.splitter)
								.append(commentStr)
								.append(Constant.splitter)
								.append("\n");

						// 如果是第一行的话
						if (firstWriteFlag) {
							// 根据最新日期设置文件名
							outputFile = MessageFormat.format(Constant.dataPattern, aGetHist.getLatestNode().getFSRQ());
							alertFile = MessageFormat.format(Constant.alertPattern, aGetHist.getLatestNode().getFSRQ());

							// 先打印表头
							FileOps.instance.outputFile(outputFile, outputHeader.toString());
							firstWriteFlag = false;
						}
					}

					// 打印到文件
					FileOps.instance.appendFile(outputFile, outputBuffer.toString());
				}
			}

			if (additionalNoteBuffer.length() == 0) {
				additionalNoteBuffer.append("")
						.append(Constant.splitter)
						.append("无")
						.append("\n");
			}
			FileOps.instance.appendFile(alertFile, additionalNoteHeaderBuffer.toString());
			FileOps.instance.appendFile(alertFile, additionalNoteBuffer.toString());

			inputStream.close();
			br.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	// public static void main(String[] args) {
	// AGetAllHistory a = new AGetAllHistory();
	// File inputFile = a.getInputFile();
	// System.out.println("========");
	// System.out.print(inputFile.getName());
	// System.out.println(inputFile.lastModified());
	// }
	public File getInputFile() {
		File file = new File(Constant.dataPath);
		File[] fileList = file.listFiles(new DataFileFilter());
		int returnFileIndex = 0;
		for (int i = 0; i < fileList.length; i++) {
			if (!fileList[i].getName().startsWith(".")) {

			}
			System.out.print(fileList[i].toPath());
			System.out.println(fileList[i].lastModified());
			if (fileList[i].lastModified() > fileList[returnFileIndex].lastModified()
					&& !fileList[i].getName().startsWith(".")) {
				returnFileIndex = i;
			}
		}
		System.out.println(fileList[returnFileIndex].toPath());
		return fileList[returnFileIndex];
	}

}
