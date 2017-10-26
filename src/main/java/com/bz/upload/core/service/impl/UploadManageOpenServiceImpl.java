package com.bz.upload.core.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.bz.dao.pojo.upload.UploadFileDetail;
import com.bz.framework.error.exception.FileException;
import com.bz.framework.pojo.page.PageBean;
import com.bz.open.core.service.upload.UploadManageOpenService;

/**
 * 
 * @author 唐鹏
 *
 */
@Service(version="1.0.0",interfaceClass=com.bz.open.core.service.upload.UploadManageOpenService.class)
public class UploadManageOpenServiceImpl implements UploadManageOpenService{
	static Log log=LogFactory.getLog(UploadManageOpenService.class);
	@Value("${upload.save.folder}")
	private String uploadSaveFolder;
	private String delExt="_del_x7_";//删除文件显示前缀
	@Autowired
	private UploadFileManager uploadFileManager;
	
	@Transactional(rollbackFor=Exception.class)
	@Override
	public int delete(long id) throws FileException {
		try {
			List<UploadFileDetail> list = uploadFileManager.queryFileDetails(id);
			if(list==null||list.size()==0){
				return 0;
			}
			uploadFileManager.delFileInfo(id);
			for (UploadFileDetail ecUploadFileDetail : list) {
				String fileurl = ecUploadFileDetail.getFileurl();
				String filepath = fileurl.substring(0,fileurl.lastIndexOf(File.separator));
				String filename = fileurl.substring(fileurl.lastIndexOf(File.separator),fileurl.length()).replace(File.separator, "");
				try {
					FileUtils.moveFile(new File(uploadSaveFolder+fileurl), new File(uploadSaveFolder+filepath+File.separator+delExt+filename));
				} catch (Exception e) {
					log.error("File delete err:'"+ecUploadFileDetail.getFileurl()+"'",e);
					throw new FileException(e);
				}
			}
			return list.size();
		} catch (Exception e) {
			log.error("delete err:",e);
			throw new FileException(e);
		}	
	}

	@Override
	public int delete(long... ids) throws FileException {
		int size = 0;
		for (long id : ids) {
			try{
				size+=delete(id);
			}catch(Exception e){
				log.error("File delete err: uploadId="+id ,e);
				throw new FileException(e);
			}
		}
		return size;
	}

	@Override
	public int delete(String ids) throws FileException {
		int size = 0;
		for (String id : ids.split(",")) {
			long uploadId = Long.parseLong(id);
			try{
				size+=delete(uploadId);
			}catch(Exception e){
				log.error("File delete err: uploadId="+id ,e);
				throw new FileException(e);
			}
		}
		return size;
	}

	@Override
	public long getTotalSize() throws FileException {
		try{
			return uploadFileManager.getTotalSize();
		}catch(Exception e){
			log.error("getTotalSize err:",e);
			throw new FileException(e);
		}
	}

	@Override
	public long getTotalQuantity() throws FileException {
		try{
			return uploadFileManager.getTotalQuantity();
		}catch(Exception e){
			log.error("getTotalQuantity err:",e);
			throw new FileException(e);
		}
	}

	@Override
	public long getTotalDeletedQuantity() throws FileException {
		try{
			return uploadFileManager.getTotalDeletedQuantity();
		}catch(Exception e){
			throw new FileException(e);
		}
	}

	@Override
	public List<UploadFileDetail> selectFileList(PageBean pb) throws FileException {
		
		return uploadFileManager.selectUploadFileDetail(pb);
	}

	
	
}
