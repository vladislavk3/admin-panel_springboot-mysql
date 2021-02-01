package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.MediaFile;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.MediaFileRepository;
import fr.be.your.self.service.MediaFileService;
import fr.be.your.self.util.StringUtils;

@Service
public class MediaFileServiceImpl  extends BaseServiceImpl<MediaFile, Integer> implements MediaFileService {

	@Autowired
	MediaFileRepository repository;
	
	@Override
	public String getDefaultSort() {
		return "filePath|asc";
	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		} else 
			throw new UnsupportedOperationException("Not yet support count with text");
	}

	@Override
	protected BaseRepository<MediaFile, Integer> getRepository() {
		return repository;
	}

	@Override
	protected Iterable<MediaFile> getList(String text, Sort sort) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(sort);
		} else 
			throw new UnsupportedOperationException("Not yet support getList with text");
	}

	@Override
	protected Page<MediaFile> getListByPage(String text, Pageable pageable) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(pageable);
		} else 
			throw new UnsupportedOperationException("Not yet support getListByPage with text");
		
	}

}
