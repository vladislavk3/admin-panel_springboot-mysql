function previewImage(inputElement, previewElementId) {
	if (!inputElement.files || !inputElement.files[0]) {
		return;
	}
		
	var reader = new FileReader();
	
	reader.onload = function(e) {
		$('#' + previewElementId).attr('src', e.target.result);
	}
	
	reader.readAsDataURL(inputElement.files[0]);
}

function validateAndPreviewImage(inputElement, 
		previewElementId, labelElementId, defaultLabel, 
		supportFileTypes, maxFileSize, fileTypeModalId, fileSizeModalId) {
	
	if (!inputElement.files || !inputElement.files[0]) {
		return null;
	}
	
	var file = inputElement.files[0];
	var fileType = file.type;
    var fileSize = file.size;
    var fileName = file.name;
    
    if (supportFileTypes && supportFileTypes.indexOf(fileType) < 0) {
    	resetImageElement(inputElement, previewElementId, labelElementId, defaultLabel);
    	
    	showModal(fileTypeModalId);
    	
    	return null;
    }
    
    if (maxFileSize && maxFileSize > 0 && fileSize > maxFileSize) {
    	resetImageElement(inputElement, previewElementId, labelElementId, defaultLabel)
    	
    	showModal(fileSizeModalId);
    	
    	return null;
    }
    
    previewImage(inputElement, previewElementId);
    
    if (labelElementId) {
		$('#' + labelElementId).text(fileName);
	}
    
    return fileName;
}

function resetImageElement(inputElement, previewElementId, labelElementId, defaultLabel) {
	inputElement.value = null;
	
	if (labelElementId && defaultLabel) {
		$('#' + labelElementId).text(defaultLabel);
	}
	
	if (previewElementId) {
		var defaultSrc = $(inputElement).attr('default-src');
		
		if (defaultSrc) {
			$('#' + previewElementId).attr('src', defaultSrc);
		}
	}
}

function validateAndPreviewMedia(inputElement, 
		audioPlayer, audioContainerElementId, audioPlayerElementId, 
		audioSupportFileTypes, audioSupportExtensions, 
		videoPlayer, videoContainerElementId, videoPlayerElementId, 
		videoSupportFileTypes, videoSupportExtensions,
		labelElementId, defaultLabel, 
		supportFileTypes, maxFileSize, fileTypeModalId, fileSizeModalId) {
	
	if (!inputElement.files || !inputElement.files[0]) {
		return null;
	}
	
	if (audioPlayer != null) {
		audioPlayer.stop();
		$('#' + audioContainerElementId).hide();
	}

	if (videoPlayer != null) {
		videoPlayer.stop();
		$('#' + videoContainerElementId).hide();
	}

	const file = inputElement.files[0];
	const fileType = file.type;
    const fileSize = file.size;
    const fileName = file.name;
	
    if (supportFileTypes && supportFileTypes.indexOf(fileType) < 0) {
    	resetMediaElement(inputElement, 
			audioPlayer, audioContainerElementId, audioPlayerElementId, audioSupportExtensions,  
			videoPlayer, videoContainerElementId, videoPlayerElementId, videoSupportExtensions,
			labelElementId, defaultLabel)
    	
    	showModal(fileTypeModalId);
    	
    	return null;
    }
	
	if (maxFileSize && maxFileSize > 0 && fileSize > maxFileSize) {
    	resetMediaElement(inputElement, 
			audioPlayer, audioContainerElementId, audioPlayerElementId, audioSupportExtensions,  
			videoPlayer, videoContainerElementId, videoPlayerElementId, videoSupportExtensions,
			labelElementId, defaultLabel)
    	
    	showModal(fileSizeModalId);
    	
    	return null;
	}
	
	if (audioSupportFileTypes && audioSupportFileTypes.indexOf(fileType) >= 0) {
		loadLocalAudioSource(audioPlayer, audioContainerElementId, 
			audioPlayerElementId, file);
	} else if (videoSupportFileTypes && videoSupportFileTypes.indexOf(fileType) >= 0) {
		loadLocalVideoSource(videoPlayer, videoContainerElementId, 
			videoPlayerElementId, file);
	} else {
		resetMediaElement(inputElement, 
			audioPlayer, audioContainerElementId, audioPlayerElementId, audioSupportExtensions,  
			videoPlayer, videoContainerElementId, videoPlayerElementId, videoSupportExtensions,
			labelElementId, defaultLabel)
    	
    	showModal(fileTypeModalId);
    	
    	return null;
	}

	if (labelElementId) {
		$('#' + labelElementId).text(fileName);
	}
    
    return fileName;
}

function resetMediaElement(inputElement, 
		audioPlayer, audioContainerElementId, audioPlayerElementId, audioSupportExtensions,  
		videoPlayer, videoContainerElementId, videoPlayerElementId, videoSupportExtensions,
		labelElementId, defaultLabel) {
	
	inputElement.value = null;
	
	if (labelElementId && defaultLabel) {
		$('#' + labelElementId).text(defaultLabel);
	}
	
	const defaultSrc = $(inputElement).attr('default-src');
	if (defaultSrc) {
		const defaultMimeType = $(inputElement).attr('default-mime-type');
		const defaultExt = getFileExtension(defaultSrc);
		
		if (defaultExt && defaultExt.length > 0) {
			if (audioSupportExtensions && audioSupportExtensions.indexOf(defaultExt) >= 0) {
				changeAudioSource(audioPlayer, audioContainerElementId, 
					audioPlayerElementId, defaultSrc, defaultMimeType);
			} else if (videoSupportExtensions && videoSupportExtensions.indexOf(defaultExt) >= 0) {
				changeVideoSource(videoPlayer, videoContainerElementId, 
					videoPlayerElementId, defaultSrc, defaultMimeType);
			}
		}
	}
}

function loadLocalAudioSource(audioPlayer, audioContainerElementId, audioPlayerElementId, file) {
	const audioMimeType = file.type;
	
	const reader = new FileReader();
	reader.onload = function(e) {
		changeAudioSource(audioPlayer, audioContainerElementId, 
			audioPlayerElementId, e.target.result, audioMimeType);
	}
	
	reader.readAsDataURL(file);
}

function loadLocalVideoSource(videoPlayer, videoContainerElementId, videoPlayerElementId, file) {
	const videoMimeType = file.type;

	const reader = new FileReader();
	reader.onload = function(e) {
		changeVideoSource(videoPlayer, videoContainerElementId, 
			videoPlayerElementId, e.target.result, videoMimeType);
	}
	
	reader.readAsDataURL(file);
}

function changeAudioSource(audioPlayer, audioContainerElementId, 
	audioPlayerElementId, audioSource, audioMimeType) {
	
	$('#' + audioContainerElementId).show();

	audioPlayer.source = {
		type: 'audio',
		sources: [
			{
				src: audioSource,
				type: audioMimeType
			}
		]
	};
}

function changeVideoSource(videoPlayer, videoContainerElementId, 
	videoPlayerElementId, videoSource, videoMimeType) {
	
	$('#' + videoContainerElementId).show();

	videoPlayer.source = {
		type: 'video',
		sources: [ 
			{
				src: videoSource,
				type: videoMimeType
			}
		]
	};
}

function showModal(modalId, hideInteval) {
	if (modalId && modalId != '') {
		$('#' + modalId).modal({ show: true });
		
		if (hideInteval && hideInteval > 0) {
			setTimeout(function() {
				$('#' + modalId).modal('hide')}
			,hideInteval);
		}
	}
}

function showOnLoadedModal() {
	showModal('on-load-modal', 3000);
}

function formatBytes(bytes, decimals) {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function formatDuration(timeInSeconds) {
	const pad = function(num, size) { return ('000' + num).slice(size * -1); };
	
	const time = parseFloat(timeInSeconds).toFixed(3);
	const hours = Math.floor(timeInSeconds / 60 / 60);
	const minutes = Math.floor(timeInSeconds / 60) % 60;
	const seconds = Math.floor(timeInSeconds - minutes * 60);
	const milliseconds = time.slice(-3);
	
	if (hours > 0) {
		return pad(hours, 2) + ':' + pad(minutes, 2) + ':' + pad(seconds, 2);
	}
    
	return pad(minutes, 2) + ':' + pad(seconds, 2);
}

function getFileExtension(fileName) {
	var lastDot = fileName.lastIndexOf('.');
	if (lastDot < 0) 
		return null;
	
	return fileName.substring(lastDot + 1);
}

function buildDataTable(dataTableId, sortableColumnNameMaps, defaultSort, sortUrl) {
	const tableSorts = [];
	
	const defaultSortProperties = defaultSort.split(';');
	for (var i = 0; i < defaultSortProperties.length; i++) {
		const defaultSortValues = defaultSortProperties[i].split('|');
		if (defaultSortValues.length == 2) {
			const columnIndex = sortableColumnNameMaps[defaultSortValues[0]];
			if (columnIndex != null && columnIndex != undefined && columnIndex >= 0) {
				tableSorts.push([columnIndex, defaultSortValues[1]]);
			}
		}
	}
	
	const sortableColumnIndexMaps = {};
	for (var columnName in sortableColumnNameMaps) {
		if (sortableColumnNameMaps.hasOwnProperty(columnName)) {
			sortableColumnIndexMaps[sortableColumnNameMaps[columnName]] = columnName;
		}
	}
	
	const tableOption = {
			"processing": true,
			"serverSide": false,
			"order": tableSorts,
			"sDom":  't',
			"columnDefs": [ {
				"targets": 'no-sort',
				"orderable": false,
			}]
	};
	
	$('#' + dataTableId)
		.DataTable(tableOption)
		.on('order.dt',  function (e, settings, ordArr) {
			var sortQuery = '';
			
			if (ordArr && ordArr.length > 0) {
				for (var i = 0; i < ordArr.length; i++) {
					const order = ordArr[i];
					const columnName = sortableColumnIndexMaps[order.col];
					
					if (columnName && columnName != '') {
						if (sortQuery.length > 0) {
							sortQuery += ';';
						}
						
						sortQuery += (columnName + '|' + order.dir);
					}
				}
			}
			
			if (defaultSort == sortQuery) {
				return;
			}
			
			const pageUrl = sortUrl + encodeURIComponent(sortQuery);
			window.location = pageUrl;
		});
}