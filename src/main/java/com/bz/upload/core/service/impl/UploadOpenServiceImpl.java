package com.bz.upload.core.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.alibaba.dubbo.config.annotation.Service;
import com.bz.dao.pojo.upload.UploadFileDetail;
import com.bz.dao.pojo.upload.UploadFileInfo;
import com.bz.framework.constant.image.WatermarkPostion;
import com.bz.framework.constant.upload.UploadBusinessType;
import com.bz.framework.constant.upload.UploadFileFormat;
import com.bz.framework.error.exception.FileException;
import com.bz.framework.util.http.JsonUtil;
import com.bz.framework.util.image.ImageUtils;
import com.bz.framework.vo.upload.UploadFileReader;
import com.bz.open.core.vo.upload.UploadVo;
/**
 * 
 * 作者: 唐鹏
 * 描述: 上传文件开放接口实现类
 * 创建时间:2017年9月30日下午5:40:17
 * 修改备注:
 */
@Service(version="1.0.0",interfaceClass=com.bz.open.core.service.upload.UploadOpenService.class)
public class UploadOpenServiceImpl extends BaseUploadOpenService {
	@Value("${upload.view.server}")
	private String serverUrl;	
	@Value("${upload.watermark.folder}")
	private String watermarkImgFolder;
	@Value("${upload.watermark.data}")
	private String watermarkImgData;
	@Autowired
	private UploadFileManager uploadFileManager;
	
	private static String HTTP="http://";
	
	private Map<String, String> watermarkImg;
	
	public UploadOpenServiceImpl() {
		
	}
	@Override
	protected UploadVo handleUpload(UploadBusinessType uploadBusinessType,
			String realPath, String contextPath,WatermarkPostion watermarkPostion,String apiClientNo) throws Exception {
		log.debug(uploadBusinessType);
		log.debug(realPath);
		log.debug(contextPath);
		log.debug(watermarkPostion);
		log.debug(apiClientNo);
		log.info("handle upload file");
		UploadVo uploadVo = processFile(uploadBusinessType, realPath, contextPath, watermarkPostion, apiClientNo);
		return uploadVo;
	}

	@Override
	public String getUrl(long id, UploadFileFormat uploadFileFormat) 
			throws FileException {
		try{
			Assert.isTrue(id>0, "参数id不能为"+id);
			UploadFileDetail data = uploadFileManager.queryFileDetail(id, uploadFileFormat.getKey());
			if(data!=null){
				String url = data.getServerurl()+data.getFileurl();
				return HTTP+url;
			}
			return null;
		}catch(Exception e){
			log.error("getUrl err:",e);
			throw new FileException(e);
		}
		
	}

	@Override
	public List<UploadVo> getUrls(
			UploadFileFormat uploadFileFormat, long... ids)
			throws FileException {
		List<UploadVo> list = new ArrayList<UploadVo>();
		UploadVo uploadVo = null;
		for (long id : ids) {
			if(id>0){
				uploadVo = new UploadVo();
				uploadVo.setId(id);
				uploadVo.setUrl(getUrl(id, uploadFileFormat));
				list.add(uploadVo);
			}
		}
		return list;
	}

	@Override
	public List<UploadVo> getUrlByIds(
			UploadFileFormat uploadFileFormat, String ids)
			throws FileException {
		try{
			Assert.notNull(ids,"ids is empty!");
			List<UploadVo> retturnlist = new ArrayList<UploadVo>();
			UploadVo uploadVo;
			for (String id : ids.split(",")) {
				if(!StringUtils.isEmpty(id)){
					long uploadId = Long.parseLong(id);				
					String url = getUrl(uploadId, uploadFileFormat);
					uploadVo = new UploadVo();
					uploadVo.setId(uploadId);
					uploadVo.setUrl(url);
					retturnlist.add(uploadVo);
				}
				
			}
			log.debug(retturnlist);
			return retturnlist;
		}catch(Exception e){
			log.error("getUrlByIds err:",e);
			throw new FileException(e);
		}
		
	}
	
	
	/**
	 * 处理文件
	 * @param uploadBusinessType
	 * @param realPath
	 * @param contextPath
	 * @param watermarkPostio
	 * @param apiClientNo
	 * @return
	 * @throws Exception 
	 */
	private final UploadVo processFile(UploadBusinessType uploadBusinessType,
			String realPath, String contextPath,WatermarkPostion watermarkPostio,String apiClientNo) throws Exception{
		try {
			File file = new File(realPath);
			String extName = file.getName().substring(file.getName().lastIndexOf(".")+1,file.getName().length());
		
			//保存原图信息到数据库
			UploadFileInfo uploadFileInfo = new UploadFileInfo();
			uploadFileInfo.setApiclientno(apiClientNo);
			uploadFileInfo.setUploadtypeid(Integer.valueOf(uploadBusinessType.getKey()+""));
			uploadFileInfo.setExtname(extName);
			uploadFileInfo.setSize(file.length());
			uploadFileInfo.setServerurl(serverUrl);
			uploadFileInfo.setBasefileurl(contextPath);	
			log.debug(uploadFileInfo);
			uploadFileManager.saveFileInfo(uploadFileInfo);
			long uploadFileId = uploadFileInfo.getUploadfileid();
			UploadVo uploadVo = null;;
			if(uploadFileId>0){
				//根据上传文件业务类型处理并保存图片详细信息
				uploadVo = saveFileDetail(file, contextPath, uploadFileId, uploadBusinessType, watermarkPostio, apiClientNo);
			}
			log.debug(uploadVo);
			return uploadVo;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("saveFileInfo", e);
			throw new Exception(e);
		}		
		
	}
	
	/**
	 *  保存图片详情
	 * @param file
	 * @param uploadType
	 * @param uploadFileId
	 * @param apiClientNo
	 * @param isWatermark
	 * @param watermarkPosition 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private UploadVo saveFileDetail(File file,String contextPath,long uploadFileId,UploadBusinessType uploadBusinessType,WatermarkPostion watermarkPostio,String apiClientNo){
		try{
			List<UploadFileDetail> fileDetails = new ArrayList<UploadFileDetail>();
			UploadFileDetail detail = null;
			String extName = file.getName().substring(file.getName().lastIndexOf(".")+1,file.getName().length());
			UploadVo uploadVo = new UploadVo();
			uploadVo.setId(uploadFileId);
			uploadVo.setUrl(HTTP+serverUrl+contextPath);
			//处理文件类型
			if(uploadBusinessType==UploadBusinessType.DOCUMENT){
				/**
				 * 所有文件类型，如：txt、doc、jpg
				 * 都默认按原始信息生成详细信息 与UploadFileFormat的配置数据格式无关
				 */
				detail = new UploadFileDetail();
				detail.setServerurl(serverUrl);
				detail.setExtname(extName);
				detail.setFileurl(contextPath);
				detail.setUploadfileid(uploadFileId);
				detail.setSize(file.length());			
				fileDetails.add(detail);
				uploadVo.setUrl(HTTP+serverUrl+contextPath);
				log.debug(detail);
				//保存处理详情到数据库
				uploadFileManager.saveFileDetail(fileDetails);
				return uploadVo;
			}else{
				//处理图片类型
				UploadFileFormat[] fileFormats = uploadBusinessType.getFileFormat();
				long fileSize = 0;
				int x=0;//偏移量x
				int y=0;//偏移量y
				float alpha=1f;//透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
				
				watermarkImg = JsonUtil.parseObject(watermarkImgData, HashMap.class);
				for (UploadFileFormat uploadFileFormat : fileFormats) {						
					String[] tempName = contextPath.split("\\.");
					String tempPath = tempName[0]+"_"+uploadFileFormat.getKey()+"."+tempName[1];
					String[] fileFormat = uploadFileFormat.getFormat().split("\\*");
					int width = Integer.valueOf(fileFormat[0]);
					int height = Integer.valueOf(fileFormat[1]);
					BufferedImage image = null;
					BufferedImage waterImg = null;
					String watermarkPath=null;
					if(watermarkImg.get(uploadFileFormat.getTitle())!=null&&watermarkImg.get(uploadFileFormat.getTitle()).length()>0){
						watermarkPath = watermarkImgFolder+File.separator+watermarkImg.get(uploadFileFormat.getTitle());
					}										
					//裁剪图片
					if(uploadFileFormat!=UploadFileFormat.DEFAULT&&width*height>1){
						image = ImageUtils.resize(file, width, height, false);
						if(watermarkPostio!=null&&watermarkPostio!=WatermarkPostion.NONE){
							//添加水印
							if(watermarkPath!=null){
								waterImg = ImageIO.read(new File(watermarkPath));
								image = ImageUtils.pressImage(image, waterImg, watermarkPostio.getKey(), x, y, alpha);
							}							
						}
						ImageUtils.saveImgToDisk(image, uploadSaveFolder+tempPath, 1);
					}else{
						image = ImageIO.read(file);
						//添加水印
						if(watermarkPostio!=null&&watermarkPostio!=WatermarkPostion.NONE){
							if(watermarkPath!=null){
								waterImg = ImageIO.read(new File(watermarkPath));
								image = ImageUtils.pressImage(image, waterImg, watermarkPostio.getKey(), x, y, alpha);
							}
							ImageUtils.saveImgToDisk(image, uploadSaveFolder+tempPath, 1);
						}else{
							tempPath = contextPath;
						}
						
					}
					//查找返回最小的图片
					File f = new File(uploadSaveFolder+tempPath);
					long tempsize = f.length();
					if(fileSize==0){
						fileSize = tempsize;
						uploadVo.setUrl(HTTP+serverUrl+tempPath);
					}else if(tempsize<fileSize){
						fileSize = tempsize;
						uploadVo.setUrl(HTTP+serverUrl+tempPath);
					}					
					//生成文件处理详情信息
					detail = new UploadFileDetail();
					detail.setServerurl(serverUrl);;
					detail.setExtname(extName);
					detail.setFileurl(tempPath);
					detail.setUploadfileid(uploadFileId);
					detail.setSize(fileSize);
					detail.setFileformat(Integer.valueOf(uploadFileFormat.getKey()+""));
					fileDetails.add(detail);					
				}
				//log.debug(fileDetails);
				//保存处理详情到数据库
				uploadFileManager.saveFileDetail(fileDetails);
				return uploadVo;
			}
			
		} catch(Exception e){
			log.error("saveFileDetail", e);
			throw new FileException(e);
		}
	}

	public <T> void loadUrl(UploadFileReader<T> uploadFileReader,UploadFileFormat uploadFileFormat) throws FileException {
		for(T data:uploadFileReader.getDataList()){
			Long uploadId=uploadFileReader.getUploadFileId(data);
			if(null!=uploadId && uploadId>0){
				uploadFileReader.setReadUrl(data,getUrl(uploadFileReader.getUploadFileId(data), uploadFileFormat));
			}
		}
	}
	@Override
	public Map<Long, String> getUrlsMap(UploadFileFormat uploadFileFormat,
			long... ids) throws FileException  {
		Map<Long, String> map = new HashMap<Long, String>();
		for (long id : ids) {
			if(id>0){
				map.put(id, getUrl(id, uploadFileFormat));
			}
		}
		return map;
	}
	@Override
	public Map<Long, String> getUrlsMapIds(UploadFileFormat uploadFileFormat,
			String ids) throws FileException {
		Assert.notNull(ids,"ids is empty!");
		Map<Long, String> map = new HashMap<Long, String>();
		for (String id : ids.split(",")) {
			if(!StringUtils.isEmpty(id)){
				long uploadId = Long.parseLong(id);				
				String url = getUrl(uploadId, uploadFileFormat);
				map.put(uploadId, url);
			}
		}
		return map;
	}
}
