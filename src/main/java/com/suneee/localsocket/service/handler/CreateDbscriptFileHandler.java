package com.suneee.localsocket.service.handler;

import com.myself.deployrequester.bo.DBScriptInfoForFileGenerate;
import com.myself.deployrequester.bo.TotalDBScriptInfoForFileGenerate;
import com.rabbitmq.client.Channel;
import com.suneee.localsocket.bo.RabbitMQConf;
import com.suneee.localsocket.globaldata.GlobalDataConf;
import com.suneee.localsocket.util.FileUtil;
import com.suneee.localsocket.util.RabbitMQUtil;
import com.suneee.localsocket.util.StringOperUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by QueRenJie on ${date}
 */
public class CreateDbscriptFileHandler extends AbstractHandler {
    private TotalDBScriptInfoForFileGenerate totalDBScriptInfoForFileGenerate;
    private String subject;
    private Map<String, RabbitMQConf> rabbitMQConfMap;
    private List<String> resultStatusInfoList;

    public CreateDbscriptFileHandler(TotalDBScriptInfoForFileGenerate totalDBScriptInfoForFileGenerate, String subject, Map<String, RabbitMQConf> rabbitMQConfMap) {
        this.totalDBScriptInfoForFileGenerate = totalDBScriptInfoForFileGenerate;
        this.subject = subject;
        this.rabbitMQConfMap = rabbitMQConfMap;
        this.resultStatusInfoList = new ArrayList<String>();
    }

    /**
     * 反馈脚本处理完后的消息
     */
    @Override
    protected void feedbackToServer() {
        //给服务器端回馈消息
        RabbitMQUtil rabbitMQUtil = new RabbitMQUtil(rabbitMQConfMap);
        try {
            rabbitMQUtil.sendStatusInfo(subject, resultStatusInfoList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理脚本文件
     */
    @Override
    protected void handleDBScriptFile() {
        Set<String> dirs = getDirs();
        if (dirs != null && dirs.size() > 0) {
            Iterator<String> iterator = dirs.iterator();
            while (iterator.hasNext()) {
                String dir = iterator.next();
                //svn update更新本地的目录
                svnUpdateDirs(dir);
                //创建和返回日期的目录
                String newDir = createAndGetDir(dir);
                System.out.println("生成目录" + newDir);
                //列出目录下的文件名
                List<String> fileNameList = getFileNameList(newDir);
                System.out.println("该目录下的原有文件有：");
                if (fileNameList != null) {
                    for (String fileName : fileNameList) {
                        System.out.println(fileName);
                    }
                }
                System.out.println("即将生成以下文件");
                Map<String, DBScriptInfoForFileGenerate> newFilesMap = null;
                try {
                    //newFilesMap是文件名全路径和脚本对象的对应关系，可用于生成csv文件
                    newFilesMap = generateFiles(dir, newDir, totalDBScriptInfoForFileGenerate);
                } catch (IOException e) {
                    System.out.println("生成文件失败，进程终止。");
                    resultStatusInfoList.add("生成文件失败，进程终止。");
                    e.printStackTrace();
                    return;
                }
                if (newFilesMap != null) {
                    for (Map.Entry<String, DBScriptInfoForFileGenerate> entry : newFilesMap.entrySet()) {
                        System.out.println("已生成文件：" + entry.getKey());
                        resultStatusInfoList.add("已生成文件：" + entry.getKey());
                    }
                }
                //生成csv文件
                try {
                    generateCsvFile(newDir, newFilesMap);
                    System.out.println("已生成new_generated.csv文件");
                    resultStatusInfoList.add("已生成new_generated.csv文件");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("在生成new_generated.csv文件时报错。");
                    resultStatusInfoList.add("在生成new_generated.csv文件时报错。");
                }
            }
        }

    }

    /**
     * 生成CSV文件
     * @param newDir
     * @param newFilesMap
     * @throws IOException
     */
    private void generateCsvFile(String newDir, Map<String, DBScriptInfoForFileGenerate> newFilesMap) throws IOException {
        String csvFileName = "new_generated.csv";
        List<String> contentList = new ArrayList<String>();
        if (newFilesMap != null) {
            for (Map.Entry<String, DBScriptInfoForFileGenerate> entry : newFilesMap.entrySet()) {
                String filePath = entry.getKey();
                DBScriptInfoForFileGenerate dbScriptInfoForFileGenerate = entry.getValue();

                String[] seperatedFilePath = filePath.split("/");
                String fileName = seperatedFilePath[seperatedFilePath.length - 1];
                String dir = seperatedFilePath[seperatedFilePath.length -2];
                String applier = dbScriptInfoForFileGenerate.getApplier();
                String description = dbScriptInfoForFileGenerate.getDescription();
                String dbname = dbScriptInfoForFileGenerate.getDbname();

                description = description.replaceAll(",", "，");
                description = description.replaceAll("\n", "。");
                description = description.replaceAll("\r", "。");
                System.out.println(description);

                String lineStr = "," + dir + "," + fileName + "," + dbname + "," + description + "," + applier;
                contentList.add(lineStr);
            }

            FileUtil.writeTxtFile(newDir + "/" + csvFileName, contentList);
        }
    }
    /**
     * 生成文件，返回文件名全路径和脚本对象的对应关系
     * @param parentDir
     * @param newDir
     * @param totalDBScriptInfoForFileGenerate
     * @return
     */
    private Map<String, DBScriptInfoForFileGenerate> generateFiles(String parentDir, String newDir, TotalDBScriptInfoForFileGenerate totalDBScriptInfoForFileGenerate) throws IOException {
        Map<String, DBScriptInfoForFileGenerate> resultMap = new HashMap<String, DBScriptInfoForFileGenerate>();

        //首先获取newDir下的所有文件名
        List<String> fileNameList = getFileNameList(newDir);

        List<DBScriptInfoForFileGenerate> filteredDBScriptInfoForFileGenerateList = filterDBScriptInfoForFileGenerate(parentDir, totalDBScriptInfoForFileGenerate);
        if (filteredDBScriptInfoForFileGenerateList != null) {
            for (DBScriptInfoForFileGenerate dbScriptInfoForFileGenerate : filteredDBScriptInfoForFileGenerateList) {
                String applier = dbScriptInfoForFileGenerate.getApplier();
                String dbname = dbScriptInfoForFileGenerate.getDbname();
                //生成文件名
                String newFileName = generateFileName(fileNameList, applier, dbname);
                //生成文件
                String newFilePath = newDir + "/" + newFileName;
                List<String> sqlList = dbScriptInfoForFileGenerate.getSubsqlList();
                List<String> adjustedSqlList = new ArrayList<String>();
                if (sqlList != null) {
                    for (String sql : sqlList) {
                        sql += ";";
                        adjustedSqlList.add(sql);
                    }
                }
                FileUtil.writeTxtFile(newFilePath, adjustedSqlList);
                resultMap.put(newFilePath, dbScriptInfoForFileGenerate);
            }
        }

        return resultMap;
    }

    /**
     * 获取指定目录下的所有文件名
     * @param newDir
     * @return
     */
    private List<String> getFileNameList(String newDir) {
        List<String> fileNameList = new ArrayList<String>();
        File fNewDir = new File(newDir);
        File[] files = fNewDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNameList.add(file.getName());
                }
            }
        }
        return fileNameList;
    }

    /**
     * 根据这三个参数生成文件名
     * @param fileNameList
     * @param applier
     * @param dbname
     * @return
     */
    private String generateFileName(List<String> fileNameList, String applier, String dbname) {
        int maxSeq = 0;
        if (fileNameList != null) {
            for (String fileName : fileNameList) {
                if (fileName.indexOf(applier + "_" + dbname) == 0) {
                    //如果找到了有前缀同名的文件，然后先去除文件的扩展名
                    int dotPostion = fileName.indexOf(".");
                    if (dotPostion > (applier + "_" + dbname).length()) {
                        fileName = fileName.substring(0, dotPostion);
                        String seqString = fileName.substring((applier + "_" + dbname).length() + 1);
                        int seq = Integer.parseInt(seqString);
                        if (seq > maxSeq) {
                            maxSeq = seq;
                        }
                    }
                }
            }
        }
        String strMaxSeq = String.valueOf(maxSeq + 1);
        String newFileName = applier + "_" + dbname + "_" + StringOperUtil.padLeft(strMaxSeq, 4, "0") + ".sql";

        return newFileName;
    }
    /**
     * 过滤出属于指定父目录的脚本
     * @param parentDir
     * @param totalDBScriptInfoForFileGenerate
     * @return
     */
    private List<DBScriptInfoForFileGenerate> filterDBScriptInfoForFileGenerate(String parentDir, TotalDBScriptInfoForFileGenerate totalDBScriptInfoForFileGenerate) {
        //根据父目录反向获取projectCode
        String projectCode = "";
        Map<String, String> projectCodeMapPath = GlobalDataConf.LOCAL_SOCKET_CONF.projectCodeMapPath;
        for (Map.Entry<String, String> entry : projectCodeMapPath.entrySet()) {
            String mappingDir = entry.getValue();
            if (parentDir.equalsIgnoreCase(mappingDir)) {
                projectCode = entry.getKey();
            }
        }

        if (StringUtils.isBlank(projectCode)) {
            return null;
        }

        List<DBScriptInfoForFileGenerate> filteredDBScriptInfoForFileGenerateList = new ArrayList<DBScriptInfoForFileGenerate>();
        for (DBScriptInfoForFileGenerate dbScriptInfoForFileGenerate : totalDBScriptInfoForFileGenerate.getDbScriptInfoForFileGenerateList()) {
            if (projectCode.equals(dbScriptInfoForFileGenerate.getProjectCode())) {
                filteredDBScriptInfoForFileGenerateList.add(dbScriptInfoForFileGenerate);
            }
        }

        return filteredDBScriptInfoForFileGenerateList;
    }

    /**
     * 创建和返回日期的目录
     * @param parentDir
     * @return
     */
    private String createAndGetDir(String parentDir) {
        //获取年月日，用于创建目录
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        String strMonth = month < 10 ? "0" + month : String.valueOf(month);
        String strDay = day < 10 ? "0" + day : String.valueOf(day);
        String newDir = parentDir + "/" + year + "-" + strMonth + "-" + strDay;
        //判断目录是否存在，不存在则创建此目录
        File file =new File(newDir);
        if (file.exists()) {
            if (!file.isDirectory()) {
                //有同名的文件但不是目录，则创建目录
                file.mkdir();
            }
        } else {
            file.mkdir();
        }
        return newDir;
    }

    /**
     * svn update更新本地的目录
     */
    private void svnUpdateDirs(String dir) {
        System.out.println("更新的目标目录：" + dir);
        String diskTag = dir.substring(0, dir.indexOf(":"));
        //创建doscommands.bat文件
        List<String> commandList = new ArrayList<String>();
        commandList.add(diskTag + ":");
        commandList.add("cd " + dir);
        commandList.add("svn update");
        try {
            FileUtil.writeTxtFile("doscommands.bat", commandList);
        } catch (IOException e) {
            System.out.println("写doscommands.bat文件出现异常，进程终止");
            e.printStackTrace();
            return;
        }
        runCmd("doscommands.bat");
    }

    /**
     * 运行dos命令
     * @param dosCmd
     */
    private void runCmd(String dosCmd) {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + dosCmd);//通过cmd程序执行dos命令
            //读取屏幕输出
            BufferedReader strCon = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = strCon.readLine()) != null) {
                System.out.println(line);
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取脚本存放的所有父目录
     * @return
     */
    private Set<String> getDirs() {
        Set<String> dirs = new HashSet<String>();
        Map<String, String> projectCodeMapPath = GlobalDataConf.LOCAL_SOCKET_CONF.projectCodeMapPath;
        if (totalDBScriptInfoForFileGenerate != null) {
            List<DBScriptInfoForFileGenerate> dbScriptInfoForFileGenerateList = totalDBScriptInfoForFileGenerate.getDbScriptInfoForFileGenerateList();
            if (dbScriptInfoForFileGenerateList != null) {
                for (DBScriptInfoForFileGenerate dbScriptInfoForFileGenerate : dbScriptInfoForFileGenerateList) {
                    String projectCode = dbScriptInfoForFileGenerate.getProjectCode();
                    String dir = projectCodeMapPath.get(projectCode);
                    dirs.add(dir);
                }
            }
        }
        return dirs;
    }
}
