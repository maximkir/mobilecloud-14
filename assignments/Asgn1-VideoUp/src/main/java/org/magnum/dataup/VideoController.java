/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

	private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	/**
	 * Used to generate unique id's to movies.
	 */
	private final AtomicLong idGenerator = new AtomicLong(0);
	
	/**
	 * A dictionary that holds video records.
	 */
	private Map<Long,Video> videos = new ConcurrentHashMap<>();
	
	@RequestMapping(value="/video", method=RequestMethod.GET)
	public @ResponseBody Collection<? extends Video> getVideos() {
		logger.debug("Retrieving all videos");
		return videos.values();
	}
	
	@RequestMapping(value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		long id = idGenerator.incrementAndGet();
		String dataUrl = getDataUrl(id);
		if (video.getId() == 0){
			video.setId(id);
		}
		video.setDataUrl(dataUrl);
		videos.put(video.getId(), video);
		logger.debug("Successfully added meta data for video, id: {}", id);
		return video;
	}
	
	@RequestMapping(value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus uploadVideo (
			@RequestParam("data") MultipartFile data,
			@PathVariable("id") Long id,
			HttpServletResponse response) throws IOException{
		if (!videos.containsKey(id)){
			response.setStatus(404);
		}else{
			try(InputStream in = data.getInputStream()){
				logger.debug("Starting to upload data for video id {}", id);
				Path destination = Paths.get("/video", id.toString(), "data");
				if (!Files.exists(destination)){
					Files.createDirectories(destination);
				}
				Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
				logger.debug("Succsessfully uploaded data for video id {}", id);
			}
		}
		return new VideoStatus(VideoState.READY);
	}
	
	
	@RequestMapping(value="/video/{id}/data", method=RequestMethod.GET)
	public void downloadVideo(
			@PathVariable("id") Long id,
			HttpServletResponse response) throws IOException{
		Path videoDataPath = Paths.get("/video", id.toString(), "data");
		boolean hasData = Files.exists(videoDataPath);
		if (!hasData){
			logger.debug("No data found for video id {}", id);
			response.setStatus(404);
			return;
		}
		logger.debug("Downloading data for video id {}", id);
		Files.copy(videoDataPath, response.getOutputStream());
	}
	
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }
    
    
    private String getUrlBaseForLocalServer() {
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base = 
           "http://"+request.getServerName() 
           + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
     }
	
}
