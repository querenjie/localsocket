package com.suneee.localsocket.util;

import com.alibaba.fastjson.JSON;
import com.suneee.localsocket.bo.LocalsocketConf;
import com.suneee.localsocket.globaldata.GlobalDataConf;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by QueRenJie on ${date}
 */
public class FileUtil {
    public static LocalsocketConf readConf() {
        LocalsocketConf localsocketConf = new LocalsocketConf();

        StringBuffer sb = new StringBuffer();
        try {
            FileReader fr = new FileReader(GlobalDataConf.CONF_FILE);
            int ch = 0;
            while ((ch = fr.read()) != -1) {
                sb.append((char)ch);
            }
            String content = sb.toString();
            if (StringUtils.isBlank(content)) {
                localsocketConf.setErrorMsg(GlobalDataConf.CONF_FILE + "中没内容。");
                return localsocketConf;
            }
            localsocketConf = JSON.parseObject(content, LocalsocketConf.class);
            return localsocketConf;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            localsocketConf.setErrorMsg("同级目录下找不到配置文件（" + GlobalDataConf.CONF_FILE + ")");
        } catch (IOException e) {
            e.printStackTrace();
            localsocketConf.setErrorMsg(GlobalDataConf.CONF_FILE + "中的内容格式可能有问题。");
        }
        return localsocketConf;
    }

    /**
     * 向指定的文件中写内容，之前的内容全被清除
     * @param fileName
     * @param content
     */
    public static void writeTxtFile(String fileName, List<String> content) throws IOException {
        String lineTag = System.getProperty("line.separator");
        FileWriter fw = new FileWriter(fileName);
        if (content != null) {
            for (String lineContent : content) {
                fw.write(lineContent);
                fw.write(lineTag);
            }
        }
        fw.close();
    }
}
