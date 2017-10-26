package com.bz.upload.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bz.dao.mapper.upload.UploadFileDetailMapper;
import com.bz.dao.mapper.upload.UploadFileInfoMapper;
import com.bz.dao.pojo.upload.UploadFileDetail;
import com.bz.dao.pojo.upload.UploadFileInfo;
import com.bz.framework.constant.manage.ManageServiceEnum.PageSettingEnum;
import com.bz.framework.constant.upload.UploadFileFormat;
import com.bz.framework.error.exception.FileException;
import com.bz.framework.pojo.page.PageBean;
import com.bz.framework.util.base.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
/**
 * 文件上传管理业务类
 * @author  唐鹏
 *
 */
@Service
@Transactional
public class UploadFileManager {
	static Log log=LogFactory.getLog(UploadFileManager.class);
	@Autowired
	private UploadFileDetailMapper uploadFileDetailMapper;
	@Autowired
	private UploadFileInfoMapper uploadFileInfoMapper;
	/**
	 * 获取上传文件详情信息
	 * @param uploadFileId 上传文件Id
	 * @return EcUploadFileDetail
	 * @throws Exception 
	 */
	@Cacheable(value="imgCache",key="#uploadFileId+'_'+#fileFormat")
	public UploadFileDetail queryFileDetail(long uploadFileId,int fileFormat) throws Exception{
		List<UploadFileDetail> list = uploadFileDetailMapper.queryFileDetail(uploadFileId,fileFormat);
		if(list!=null&&list.size()>0){
			return list.get(0);
		}
		return null;
	}
	/**
	 * 获取上传文件详情信息
	 * @param uploadFileId 上传文件Id
	 * @return List<EcUploadFileDetail>
	 */
	@Cacheable(value="imgCache",key="#uploadFileId")
	public List<UploadFileDetail> queryFileDetails(long uploadFileId) throws Exception{
		return uploadFileDetailMapper.queryFileDetail(uploadFileId,-1);
	}
	/**
	 * 保存文件信息
	 * @param uploadFileInfo
	 * @return
	 */
	public int saveFileInfo(UploadFileInfo uploadFileInfo) throws Exception{
		return uploadFileInfoMapper.insertSelective(uploadFileInfo);
			
	}
	/**
	 * 保存文件详情
	 * @param fileDetails
	 */
	public void saveFileDetail(List<UploadFileDetail> fileDetails) throws Exception{
		uploadFileDetailMapper.saveFileDetail(fileDetails);
	}
	/**
	 * 删除文件信息
	 * @param uploadFileId
	 * @return
	 */
	public int delFileInfo(long uploadFileId) throws Exception{
		return uploadFileDetailMapper.delById(Integer.valueOf(uploadFileId+""));
	};
	/**
	 * 删除文件信息
	 * @param ids 
	 * @return
	 */
	public int delFileInfoByIds(String ids) throws Exception{
		return uploadFileDetailMapper.delFileInfoByIds(ids);
	}
	/**
	 * 获取上传文件中大小（字节）
	 * @return
	 */
	public long getTotalSize() throws Exception{
		return uploadFileDetailMapper.getTotalSize();
	}
	/**
	 * 获取上传文件总数量 包括已经删除的文件
	 * @return
	 */
	public long getTotalQuantity() throws Exception{
		return uploadFileDetailMapper.getTotalQuantity();
	}
	/**
	 * 获取已经删除的文件总数量
	 * @return
	 */
	public long getTotalDeletedQuantity() throws Exception{
		return uploadFileDetailMapper.getTotalDeletedQuantity();
	}
	
	/**
	 * 获取上传文件URL地址
	 * @param id 上传文件Id
	 * @param uploadFileFormat 规格{@link UploadFileFormat}
	 * @return URL地址
	 * @throws UploadFileError
	 */
	@Cacheable(value="imgCache",key="#id+'_'+#uploadFileFormat.key")
	public String getUrl(long id,UploadFileFormat uploadFileFormat)throws FileException{
		
		return "";
	}
	
	/**
	 * 
	 * 作者:唐鹏
	 * 创建时间:2017年10月17日下午5:56:17
	 * 描述:分页查询系统的文件信息
	 * 备注:
	 * @return
	 * @throws FileException
	 */
	public List<UploadFileDetail> selectUploadFileDetail(PageBean pb)throws FileException{
		List<UploadFileDetail> list=new ArrayList<>();
		Page<?> page=PageHelper.startPage(pb.getCurPage(),PageSettingEnum.MANAGE_BASE_PAGE_SIZE.getKey());
		list=uploadFileDetailMapper.selectList(null);
		PageUtil.getPageNumber(pb, page);
		return list;
	}
}
