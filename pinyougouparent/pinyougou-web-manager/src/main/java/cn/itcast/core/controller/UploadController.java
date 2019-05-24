package cn.itcast.core.controller;

import entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;


    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file)throws Exception{

        String url= null;
        try {
            String filename = file.getOriginalFilename();

            System.out.println(filename);
            String extName = filename.substring(filename.lastIndexOf(".") + 1);
            FastDFSClient fastDFSClient=new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
//            String path = fastDFSClient.uploadFile(filename, extName, null);
            String ext = FilenameUtils.getExtension(filename);
            String path = fastDFSClient.uploadFile(file.getBytes(), ext, null);
            url = FILE_SERVER_URL+path;
            System.out.println(url);
        return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
        return new Result(false,"上传失败");
        }
    }
}
