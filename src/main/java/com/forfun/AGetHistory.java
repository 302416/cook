package com.forfun;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
// import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import com.util.FileOps;

public class AGetHistory {

    private String fundCode;
    private String startDate; // 格式: 2021-01-01
    private String endDate; // 格式: 2021-08-28
    private String dataFile;
    private String fixedStartDate = "2001-01-01";
    private String fixedEndDate = "2025-07-07";
    private FundHistoricData largestNode; // 净值最高节点
    private FundHistoricData largestWithdrawNode; // 回撤最大节点
    private FundHistoricData latestNode; // 最新节点
    private FundHistoricData startNode; // 最新节点

    private TreeMap<Date, FundHistoricData> allData = new TreeMap<>();
    private TreeMap<Date, FundHistoricData> processedData = new TreeMap<>();
    private TreeMap<Date, FundHistoricData> queryData = new TreeMap<>();

    private Boolean FHSPflag = false;
    StringBuffer outputStr = new StringBuffer();

    public static void main(String[] args) {
        AGetHistory a = new AGetHistory("000828", "2001-01-01", "2025-07-07");
        a.collectData();
        System.out.println("withdraw:" + a.getLatestNode().getWithdraw());
        System.out.println("highest date:" + a.getLargestNode().getFSRQ());
        System.out.println("highest value:" + a.getLargestNode().getDWJZ());
        System.out.println("largest withdraw date:" + a.getLargestWithdrawNode().getFSRQ());
        System.out.println("largest withdraw:" + a.getLargestWithdrawNode().getWithdraw());
    }

    public AGetHistory(String fundCode, String startDate, String endDate) {
        setFundCode(fundCode);
        setStartDate(startDate);
        setEndDate(endDate);
        setFilePath(fundCode);
    }

    private void setFilePath(String fundCode) {
        this.dataFile = MessageFormat.format(Constant.dataDetailPattern, fundCode);
    }

    private void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    private void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    private void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // private String getFundCode() {
    // return this.fundCode;
    // }

    // private String getStartDate() {
    // return this.startDate;
    // }

    // private String getEndDate() {
    // return this.endDate;
    // }

    public void collectData() {
        loadExitDataFile();
        refreshData();
        setWithdraw();
        queryData();
        // allData =

        // // 获取历史净值，并计算回撤
        // ArrayList processData = setWithdraw(getHisData());

        // 输出到文件，目录dataDetail下，一只基金一个文件，保存历史净值数据
        writeToFile(this.dataFile);

        System.out.println("FHSPflag:" + String.valueOf(FHSPflag));
    }

    public FundHistoricData getLargestNode() {
        return this.largestNode;
    }

    public FundHistoricData getLargestWithdrawNode() {
        return this.largestWithdrawNode;
    }

    public FundHistoricData getLatestNode() {
        return this.latestNode;
    }

    public Boolean getFHSPflag() {
        return this.FHSPflag;
    }

    public String getStartDate() {
        return FundHistoricData.dateFormat.format(this.startNode.FSRQ);
    }

    public String getEndDate() {
        return FundHistoricData.dateFormat.format(this.latestNode.FSRQ);
    }

    private void writeToFile(String filePath) {
        try {
            StringBuffer outputStr = new StringBuffer();

            // 表头
            outputStr.append("FSRQ").append(Constant.splitter)
                    .append("DWJZ").append(Constant.splitter)
                    .append("LJJZ").append(Constant.splitter)
                    .append("JZZZL").append(Constant.splitter)
                    .append("SDATE").append(Constant.splitter)
                    .append("ACTUALSYI").append(Constant.splitter)
                    .append("NAVTYPE").append(Constant.splitter)
                    .append("SGZT").append(Constant.splitter)
                    .append("SHZT").append(Constant.splitter)
                    .append("FHFCZ").append(Constant.splitter)
                    .append("FHFCBZ").append(Constant.splitter)
                    .append("DTYPE").append(Constant.splitter)
                    .append("FHSP").append(Constant.splitter)
                    .append("FQJZ").append(Constant.splitter)
                    .append("preHighFSRQ").append(Constant.splitter)
                    .append("preHighDWJZ").append(Constant.splitter)
                    .append("preHighLJJZ").append(Constant.splitter)
                    .append("withdraw").append(Constant.splitter)
                    .append("\n");
            FileOps.instance.outputFile(filePath, outputStr.toString());

            Set<Date> keySet = processedData.keySet();
            Iterator<Date> iter = keySet.iterator();
            while (iter.hasNext()) {
                Date key = iter.next();
                FundHistoricData oneDay = processedData.get(key);
                outputStr.append(oneDay.getFSRQ()).append(Constant.splitter)
                        .append(oneDay.DWJZ).append(Constant.splitter)
                        .append(oneDay.LJJZ).append(Constant.splitter)
                        .append(oneDay.JZZZL).append(Constant.splitter)
                        .append(oneDay.SDATE).append(Constant.splitter)
                        .append(oneDay.ACTUALSYI).append(Constant.splitter)
                        .append(oneDay.NAVTYPE).append(Constant.splitter)
                        .append(oneDay.SGZT).append(Constant.splitter)
                        .append(oneDay.SHZT).append(Constant.splitter)
                        .append(oneDay.FHFCZ).append(Constant.splitter)
                        .append(oneDay.FHFCBZ).append(Constant.splitter)
                        .append(oneDay.DTYPE).append(Constant.splitter)
                        .append(oneDay.FHSP).append(Constant.splitter)
                        .append(oneDay.FQJZ).append(Constant.splitter)
                        .append(oneDay.getPreHighFSRQ()).append(Constant.splitter)
                        .append(oneDay.preHighDWJZ).append(Constant.splitter)
                        .append(oneDay.preHighLJJZ).append(Constant.splitter)
                        .append(oneDay.withdraw).append(Constant.splitter)
                        .append("\n");
                FileOps.instance.outputFile(filePath, outputStr.toString());
            }
            // for (int i = 0; i < hisData.size(); i++) {
            // FundHistoricData oneDay = (FundHistoricData) hisData.get(i);
            // outputStr.append(oneDay.getFSRQ()).append(Constant.splitter)
            // .append(oneDay.DWJZ).append(Constant.splitter)
            // .append(oneDay.LJJZ).append(Constant.splitter)
            // .append(oneDay.JZZZL).append(Constant.splitter)
            // .append(oneDay.SDATE).append(Constant.splitter)
            // .append(oneDay.ACTUALSYI).append(Constant.splitter)
            // .append(oneDay.NAVTYPE).append(Constant.splitter)
            // .append(oneDay.SGZT).append(Constant.splitter)
            // .append(oneDay.SHZT).append(Constant.splitter)
            // .append(oneDay.FHFCZ).append(Constant.splitter)
            // .append(oneDay.FHFCBZ).append(Constant.splitter)
            // .append(oneDay.DTYPE).append(Constant.splitter)
            // .append(oneDay.FHSP).append(Constant.splitter)
            // .append(oneDay.FQJZ).append(Constant.splitter)
            // .append(oneDay.getPreHighFSRQ()).append(Constant.splitter)
            // .append(oneDay.preHighDWJZ).append(Constant.splitter)
            // .append(oneDay.preHighLJJZ).append(Constant.splitter)
            // .append(oneDay.withdraw).append(Constant.splitter)
            // .append("\n");
            // FileOps.instance.outputFile(filePath, outputStr.toString());
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setWithdraw() {
        Set<Date> keySet = allData.keySet();
        Iterator<Date> iter = keySet.iterator();
        int index = 0;
        float fqjz = 1.0f;
        while (iter.hasNext()) {
            Date key = iter.next();
            FundHistoricData data = allData.get(key);

            if (index == 0) {
                data.setFQJZ(data.DWJZ);
                fqjz = data.DWJZ;
            } else {
                if (FHSPflag) {
                    // 有过历史分红的，计算复权净值
                    if (data.JZZZL != null) {
                        fqjz = (float) Math.round(fqjz * (1 + Float.valueOf(data.JZZZL) / 100) * 100000) / 100000;
                    }
                } else {
                    // 没有历史分红的，复权净值 = 净值 = 累计净值
                    fqjz = data.DWJZ;
                }

                data.setFQJZ(fqjz);
            }
            processedData.put(key, data);
            index++;
        }

    }

    private void queryData() {
        try {
            Date queryStartDate = FundHistoricData.dateFormat.parse(startDate);
            Set<Date> keySet = processedData.keySet();
            Iterator<Date> iter = keySet.iterator();
            int index = 0;
            while (iter.hasNext()) {
                Date key = iter.next();
                FundHistoricData data = processedData.get(key);

                if (data.FSRQ.before(queryStartDate)) {
                    // do nothing
                } else {
                    if (index == 0) {
                        this.startNode = data;
                        this.largestNode = data;

                        data.setPreHighFSRQ(this.largestNode.FSRQ);
                        data.setPreHighDWJZ(this.largestNode.DWJZ);
                        data.setPreHighLJJZ(this.largestNode.LJJZ);
                        data.setPreHighFQJZ(this.largestNode.FQJZ);
                        data.withdraw = 0f;

                        this.largestWithdrawNode = data;
                        queryData.put(key, data);

                        // 记录最新节点
                        this.latestNode = data;
                    } else {
                        // 比较累计净值，如果当前更高，替换为this.largestNode
                        if (data.LJJZ != null && data.LJJZ > this.largestNode.LJJZ) {
                            this.largestNode = data;
                        }

                        // 记录此前最高点信息
                        data.setPreHighFSRQ(this.largestNode.FSRQ);
                        data.setPreHighDWJZ(this.largestNode.DWJZ);
                        data.setPreHighLJJZ(this.largestNode.LJJZ);
                        data.setPreHighFQJZ(this.largestNode.FQJZ);

                        // 计算当前节点回撤
                        Float withdraw = data.calculateWithdraw();
                        data.withdraw = withdraw;

                        // 替换最大回撤节点
                        if (withdraw > this.largestWithdrawNode.withdraw) {
                            this.largestWithdrawNode = data;
                        }
                        queryData.put(key, data);

                        // 记录最新节点
                        this.latestNode = data;
                    }
                    index++;
                }

            }

        } catch (ParseException e) {
            System.out.println("fundCode: " + fundCode + ";  startDate: " + startDate + "; endDate: " + endDate);
            e.printStackTrace();
        }
    }

    // // 根据历史数据计算回撤
    // /**
    // * @param hisData
    // * @return
    // */
    // private ArrayList<FundHistoricData> setWithdraw(ArrayList<FundHistoricData>
    // hisData) {
    // ArrayList<FundHistoricData> newDataList = new ArrayList();

    // // dateFormat.parse(date);
    // FundHistoricData dayOne = (FundHistoricData) hisData.get(0);
    // try {
    // Date queryStartDate = FundHistoricData.dateFormat.parse(startDate);
    // int i = 1;
    // for (; i < hisData.size(); i++) {
    // if (dayOne.FSRQ.before(queryStartDate)) {
    // dayOne = (FundHistoricData) hisData.get(i);
    // }
    // }

    // System.out.print("dayOne: ");
    // System.out.println(dayOne.FSRQ);
    // System.out.print("i: ");
    // System.out.println(i);
    // this.largestNode = dayOne;
    // float fqjz = dayOne.DWJZ;
    // dayOne.setFQJZ(dayOne.DWJZ);
    // dayOne.setPreHighFSRQ(this.largestNode.FSRQ);
    // dayOne.setPreHighDWJZ(this.largestNode.DWJZ);
    // dayOne.setPreHighLJJZ(this.largestNode.LJJZ);
    // dayOne.setPreHighFQJZ(this.largestNode.FQJZ);
    // dayOne.calculateWithdraw();
    // newDataList.add(dayOne);

    // this.largestWithdrawNode = dayOne;
    // i++;
    // for (; i < hisData.size(); i++) {
    // FundHistoricData dayTwo = (FundHistoricData) hisData.get(i);

    // // 比较累计净值，如果当前更高，替换为this.largestNode
    // if (dayTwo.LJJZ != null && dayTwo.LJJZ > this.largestNode.LJJZ) {
    // this.largestNode = dayTwo;
    // }

    // /*
    // * https://zhuanlan.zhihu.com/p/130476734
    // * 复权净值：等于基金成立以来每天的收益率加1连乘，即a(T) = [1+r(1)]*[1+r(2)]*...*[1+r(T)]，
    // * 其中r(t)就是基金投资端的实际收益率，计算公式为r(t) = U(t) / u(t-1) - 1 = [u(t)*s(t)+d(t)] /
    // u(t-1)
    // * - 1，
    // * 这里U(t)就是用拆分s(t)和分红d(t)从单位净值u(t)还原出的原始（即拆分和分红前的）单位净值。
    // * 复权净值的递推算法：a(t) = a(t-1) * [1+r(t)] = a(t-1) * [u(t)*s(t) + d(t)] / u(t-1)。
    // * 如果没发生拆分和分红，即s(t)=1且d(t)=0，那么从公式看出真实收益率r(t) = u(t)/u(t-1) - 1就是单位净值的涨幅
    // */
    // if (FHSPflag) {
    // // 有过历史分红的，计算复权净值
    // if (dayTwo.JZZZL != null) {
    // fqjz = (float) Math.round(fqjz * (1 + Float.valueOf(dayTwo.JZZZL) / 100) *
    // 100000) / 100000;
    // }
    // } else {
    // // 没有历史分红的，复权净值 = 净值 = 累计净值
    // fqjz = dayTwo.DWJZ;
    // }

    // dayTwo.setFQJZ(fqjz);

    // // 记录此前最高点信息
    // dayTwo.setPreHighFSRQ(this.largestNode.FSRQ);
    // dayTwo.setPreHighDWJZ(this.largestNode.DWJZ);
    // dayTwo.setPreHighLJJZ(this.largestNode.LJJZ);
    // dayTwo.setPreHighFQJZ(this.largestNode.FQJZ);

    // // 计算当前节点回撤
    // Float withdraw = dayTwo.calculateWithdraw();
    // dayTwo.withdraw = withdraw;
    // newDataList.add(dayTwo);
    // dayOne = dayTwo;

    // // 替换最大回撤节点
    // if (withdraw > this.largestWithdrawNode.withdraw) {
    // this.largestWithdrawNode = dayTwo;
    // }
    // }

    // // 记录最新节点
    // this.latestNode = hisData.get(hisData.size() - 1);

    // } catch (Exception e) {
    // System.out.println("fundCode: " + fundCode + "; startDate: " + startDate + ";
    // endDate: " + endDate);
    // e.printStackTrace();
    // }
    // return hisData;
    // }

    private void refreshData() {
        // 从天天读取历史数据
        int pageIndex = 1;
        JSONObject jsonObject = getContent(pageIndex);
        // System.out.println(jsonObject);
        appendData(jsonObject);

        int totalCount = (int) jsonObject.getInt("TotalCount"); // 数据条数
        int pages = totalCount / Constant.pageSize + 1;

        while (allData.size() < totalCount && pageIndex <= pages) {
            jsonObject = getContent(pageIndex);
            appendData(jsonObject);
            pageIndex++;
        }

    }

    // public ArrayList<FundHistoricData> getHisData() {
    // System.out.println("I'm going to get");

    // // 读取现有数据
    // ArrayList<FundHistoricData> histDataListExist = loadDataFile();

    // ArrayList<FundHistoricData> histDataList = new ArrayList();

    // int pageIndex = 1;

    // // 从天天读取历史数据
    // JSONObject jsonObject = getContent(pageIndex);

    // int totalCount = (int) jsonObject.getInt("TotalCount"); // 数据条数
    // int pages = totalCount / Constant.pageSize + 1;
    // System.out.print("pages: ");
    // System.out.println(pages);

    // if (histDataListExist.size() == 0) {
    // // 本地存储的历史数据为空，逐页从天天读取
    // histDataList.addAll(parseRequest(jsonObject));
    // if (pages > 1) {
    // for (int i = 2; i <= pages; i++) {
    // jsonObject = getContent(i);
    // histDataList.addAll(parseRequest(jsonObject));
    // }
    // }
    // // 颠倒顺序，按日期升序存放
    // for (int i = histDataList.size() - 1; i > -1; i--) {
    // histDataListExist.add(histDataList.get(i));
    // validFHSP(histDataList.get(i));
    // }
    // } else {
    // histDataList.addAll(parseRequest(jsonObject));

    // // 根据已有数据量和总数据条数判断是否接着从天天读取
    // while ((histDataListExist.size() + histDataList.size()) < totalCount) {
    // pageIndex++;
    // jsonObject = getContent(pageIndex);
    // histDataList.addAll(parseRequest(jsonObject));
    // }

    // // 获取已有数据的最新日期
    // Date existLatest = histDataListExist.get(histDataListExist.size() - 1).FSRQ;
    // // 把新数据添加到队尾
    // for (int i = histDataList.size() - 1; i > -1; i--) {
    // if (histDataList.get(i).FSRQ.getTime() > existLatest.getTime()) {
    // histDataListExist.add(histDataList.get(i));
    // }
    // }
    // // 验证是否有历史分红
    // for (int i = 0; i < histDataListExist.size(); i++) {
    // validFHSP(histDataListExist.get(i));
    // }
    // }
    // return histDataListExist;
    // }

    private void validFHSP(FundHistoricData data) {
        if (data.FHSP != null && data.FHSP.trim().length() != 0) {
            this.FHSPflag = true;
        }

    }

    // // 读取现有数据
    // private ArrayList<FundHistoricData> loadDataFile() {
    // ArrayList<FundHistoricData> dataList = new ArrayList();
    // try {
    // InputStream input = new FileInputStream(this.dataFile);
    // BufferedReader br = new BufferedReader(new InputStreamReader(input));
    // String line = null;
    // br.readLine();
    // while ((line = br.readLine()) != null) {
    // String[] items = line.split(",");
    // FundHistoricData oneData = new FundHistoricData(
    // items[0],
    // items[1],
    // items[2],
    // items[3],
    // items[4],
    // items[5],
    // items[6],
    // items[7],
    // items[8],
    // items[9],
    // items[10],
    // items[11],
    // items[12]);
    // dataList.add(oneData);
    // // allData.put(oneData.FSRQ, oneData);
    // }

    // input.close();
    // br.close();
    // } catch (IOException e) {
    // System.out.println(e.getMessage());
    // }
    // return dataList;
    // }

    // 读取现有数据
    private void loadExitDataFile() {
        try {
            InputStream input = new FileInputStream(this.dataFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");
                FundHistoricData oneData = new FundHistoricData(
                        items[0],
                        items[1],
                        items[2],
                        items[3],
                        items[4],
                        items[5],
                        items[6],
                        items[7],
                        items[8],
                        items[9],
                        items[10],
                        items[11],
                        items[12]);

                allData.put(oneData.FSRQ, oneData);
                validFHSP(oneData);
            }

            input.close();
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // private ArrayList<FundHistoricData> parseRequest(JSONObject jsonObject) {
    // ArrayList<FundHistoricData> dataList = new ArrayList();

    // JSONObject jsonObjData = jsonObject.getJSONObject("Data");
    // JSONArray jsonArrayDataList = jsonObjData.getJSONArray("LSJZList");
    // for (int i = 0; i < jsonArrayDataList.size(); i++) {
    // JSONObject oneRecord = (JSONObject) jsonArrayDataList.get(i);
    // FundHistoricData oneData = new FundHistoricData(
    // oneRecord.getString("FSRQ"),
    // oneRecord.getString("DWJZ"),
    // oneRecord.getString("LJJZ"),
    // oneRecord.getString("JZZZL"),
    // oneRecord.getString("SDATE"),
    // oneRecord.getString("ACTUALSYI"),
    // oneRecord.getString("NAVTYPE"),
    // oneRecord.getString("SGZT"),
    // oneRecord.getString("SHZT"),
    // oneRecord.getString("FHFCZ"),
    // oneRecord.getString("FHFCBZ"),
    // oneRecord.getString("DTYPE"),
    // oneRecord.getString("FHSP"));
    // dataList.add(oneData);
    // }
    // System.out.println(dataList.size());
    // return dataList;
    // }

    private void appendData(JSONObject jsonObject) {
        // ArrayList<FundHistoricData> dataList = new ArrayList();

        JSONObject jsonObjData = jsonObject.getJSONObject("Data");
        JSONArray jsonArrayDataList = jsonObjData.getJSONArray("LSJZList");
        for (int i = 0; i < jsonArrayDataList.size(); i++) {
            JSONObject oneRecord = (JSONObject) jsonArrayDataList.get(i);
            FundHistoricData oneData = new FundHistoricData(
                    oneRecord.getString("FSRQ"),
                    oneRecord.getString("DWJZ"),
                    oneRecord.getString("LJJZ"),
                    oneRecord.getString("JZZZL"),
                    oneRecord.getString("SDATE"),
                    oneRecord.getString("ACTUALSYI"),
                    oneRecord.getString("NAVTYPE"),
                    oneRecord.getString("SGZT"),
                    oneRecord.getString("SHZT"),
                    oneRecord.getString("FHFCZ"),
                    oneRecord.getString("FHFCBZ"),
                    oneRecord.getString("DTYPE"),
                    oneRecord.getString("FHSP"));
            // dataList.add(oneData);
            allData.put(oneData.FSRQ, oneData);
            validFHSP(oneData);
        }
        // System.out.println(dataList.size());
        // return dataList;
    }

    // 从天天读取历史数据
    private JSONObject getContent(int pageIndex) {
        JSONObject jsonObject = null;
        try {
            // 间隔1秒读，没原因，就是担心太频繁被屏蔽
            java.lang.Thread.sleep(1000);

            String uriPattern = "http://api.fund.eastmoney.com/f10/lsjz?callback=jQuery18306188109649070744_1657062132939&fundCode={0}&pageIndex={1}&pageSize={2}&startDate={3}&endDate={4}&_=1657062161019";
            String uri = MessageFormat.format(uriPattern, fundCode, pageIndex, Constant.pageSize, fixedStartDate,
                    fixedEndDate);

            // 需要加header，不然要失败
            Request req = Request.Get(uri).setHeader("Referer", "http://fundf10.eastmoney.com/");

            Response response = req.execute();
            String content = response.returnContent().toString();

            content = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
            jsonObject = JSONObject.fromObject(content);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonObject;
    }
}
