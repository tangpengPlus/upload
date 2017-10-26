package com.bz.upload.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import com.bz.framework.constant.image.WatermarkPostion;
import com.bz.framework.constant.result.ResultValueEnum;
import com.bz.framework.constant.upload.UploadBusinessType;
import com.bz.framework.error.exception.FileException;
import com.bz.framework.pojo.base.BaseResult;
import com.bz.framework.util.upload.UpLoadUtil;
import com.bz.framework.util.validate.TwoItem;
import com.bz.framework.vo.upload.UploadFile;
import com.bz.framework.vo.upload.UploadFileBASE64;
import com.bz.open.core.service.upload.UploadOpenService;
import com.bz.open.core.vo.upload.UploadVo;
/**
 * 上传文件开放接口实现基类
 * @author 唐鹏
 *
 */
public abstract class BaseUploadOpenService implements UploadOpenService {
	static Log log=LogFactory.getLog(BaseUploadOpenService.class);
	@Value("${upload.save.folder}")
	protected String uploadSaveFolder;
	
	public BaseUploadOpenService() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public BaseResult<UploadVo> uploadByBASE64(WatermarkPostion watermarkPostion,String apiClientNo,UploadFileBASE64 upFileBASE64)throws FileException{
		InputStream inputStream = null;
		BaseResult<UploadVo> baseResult = BaseResult.newInstance();
		try {
			Assert.notNull(upFileBASE64);
			Assert.notNull(upFileBASE64.getFile());			
			//log.debug(upFileBASE64);
			inputStream = upFileBASE64.getInputStream();
			//上传文件检查（base64）
			if(StringUtils.isEmpty(upFileBASE64.getFile())){
				return BaseResult.valueOf(ResultValueEnum.UPLOAD_NULL_ERROR);
			}
			//必填参数检查
			if(upFileBASE64.getUploadBusinessType()==null||upFileBASE64.getFileSize()==0){
				return BaseResult.valueOf(ResultValueEnum.UPLOAD_ALLOW_TYPE_ERROR);
			}
			//检查文件大小
			if(inputStream.available()>upFileBASE64.getFileSize()){
				return BaseResult.valueOf(ResultValueEnum.UPLOAD_SIZE_ERROR);
			}			
			String extName=upFileBASE64.getExtName();
			boolean flag = false;
			if(!StringUtils.isEmpty(extName)){
				for(String type : upFileBASE64.getUploadBusinessType().getAllowFileType()){
					if(extName.equals(type)){
						flag=true;
					}
				}
			}
			//检查文件类型
			if(!flag){
				return BaseResult.valueOf(ResultValueEnum.UPLOAD_FORMAT_ERROR);
			}
			
			TwoItem<String, String> saveFolder=upFileBASE64.getUploadBusinessType().getSaveFolder(uploadSaveFolder);
			if(StringUtils.isEmpty(extName)){
				extName = ".jpg";
			}
			String fileName=UpLoadUtil.createFileName(extName);
			log.debug(saveFolder);
			log.debug(fileName);
			
			FileUtils.copyInputStreamToFile(inputStream, new File(saveFolder.first+fileName));
			UploadVo uploadVo = handleUpload(upFileBASE64.getUploadBusinessType(),saveFolder.first+fileName,saveFolder.second+fileName,watermarkPostion,apiClientNo);
			baseResult.setData(uploadVo);
			return baseResult;
		} catch (Exception e) {
			log.error("uploadBASE64",e);
			closeInputStream(inputStream);
			throw new FileException(e);
		}finally{
			closeInputStream(inputStream);
		}
	}
	
	@Override
	public UploadVo upload(WatermarkPostion watermarkPostion, String apiClientNo,UploadFile upFile, InputStream inputStream
			) throws FileException {
		try {
			if(StringUtils.isEmpty(upFile)){
				Assert.notNull(upFile);
			}
			Assert.notNull(inputStream);
			//log.debug(upFile);
			TwoItem<String, String> saveFolder=upFile.getUploadBusinessType().getSaveFolder(uploadSaveFolder);
			String extName = ".jpg";
			if(!StringUtils.isEmpty(upFile.getMultipartFile())){
				extName = upFile.getExtName();
			}
			String fileName=UpLoadUtil.createFileName(extName);
			log.debug(saveFolder);
			log.debug(fileName);
			FileUtils.copyInputStreamToFile(inputStream, new File(saveFolder.first+fileName));
			return handleUpload(upFile.getUploadBusinessType(),saveFolder.first+fileName,saveFolder.second+fileName,watermarkPostion,apiClientNo);
		} catch (Exception e) {
			log.error("uploadBase64",e);
			closeInputStream(inputStream);
			throw new FileException(e);
		}finally{
			closeInputStream(inputStream);
		}
	}
	private void closeInputStream(InputStream inputStream){
		if(null!=inputStream){
			try {
				inputStream.close();
				inputStream=null;
			} catch (IOException e) {
			}
		}
	}
	/**
	 * 处理上传文件
	 * @param UploadBusinessType 上传文件业务类型
	 * @param realPath 上传文件保存的物理完整路径
	 * @param contextPath 上传文件保存的相对路径
	 * @param watermarkPostio 水印位置 {@link WatermarkPostion}
	 * @return 上传源文件保存后结果{@link UploadVo}
	 * @throws Exception
	 */
	protected abstract UploadVo handleUpload(UploadBusinessType uploadBusinessType,String realPath,String contextPath,WatermarkPostion watermarkPostion,String apiClientNo) throws Exception;
	
}
