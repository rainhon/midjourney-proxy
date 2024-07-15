package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.domain.DomainObject;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("任务")
public class Task extends DomainObject {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	@ApiModelProperty("任务类型")
	private TaskAction action;
	@ApiModelProperty("任务状态")
	private TaskStatus status = TaskStatus.NOT_START;

	@ApiModelProperty("提示词")
	private String prompt;
	@ApiModelProperty("提示词-英文")
	private String promptEn;

	@ApiModelProperty("任务描述")
	private String description;
	@ApiModelProperty("自定义参数")
	private String state;

	@ApiModelProperty("提交时间")
	private Long submitTime;
	@ApiModelProperty("开始执行时间")
	private Long startTime;
	@ApiModelProperty("结束时间")
	private Long finishTime;

	@ApiModelProperty("图片url")
	private String imageUrl;

	@ApiModelProperty("任务进度")
	private String progress;
	@ApiModelProperty("失败原因")
	private String failReason;

	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}

	public String getImageUrl() {
		if (this.status == TaskStatus.SUCCESS) {
			try {
				String contents = this.readFileFromUrl(this.imageUrl);
				log.error("upload");
				return this.uploadToUrl("https://image.hbkj.vip/upload.php", contents, "files");
			} catch (Exception e) {
				log.error("upload error" + e.getMessage());
				return this.imageUrl;
			}

		} else {
			log.error("no upload");
			return this.imageUrl;
		}
	}

	private String readFileFromUrl(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        reader.close();

        return stringBuilder.toString();
    }

	   // 将内容上传到URL (使用multipart/form-data格式)
    private String uploadToUrl(String uploadUrl, String fileContents, String fileName) throws Exception {
        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");

        OutputStream outputStream = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        // 写入文件内容
        writer.append("--*****\r\n")
              .append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n")
              .append("Content-Type: text/plain\r\n\r\n")
              .append(fileContents)
              .append("\r\n");

        // 添加结束标识
        writer.append("--*****--\r\n");
        writer.flush();
        writer.close();

        // 获取服务器的响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        reader.close();

        conn.disconnect();

        // 返回url
        return responseBuilder.toString();
    }
}
