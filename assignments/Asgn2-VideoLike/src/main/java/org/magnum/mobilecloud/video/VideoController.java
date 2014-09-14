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

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.Like;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VideoController {
	
	private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	@Autowired
	private VideoRepository videos;
	
	@RequestMapping(value="/video", method=RequestMethod.GET)
	public @ResponseBody Collection<? extends Video> getVideos() {
		logger.debug("Retrieving all videos");
		Collection<Video> allVideos = new LinkedList<>();
		for(Video v : videos.findAll()){
			allVideos.add(v);
		}
		return allVideos;
	}
	
	@RequestMapping(value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		return videos.save(video);
	}
	
	@RequestMapping(value="/video/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideo(@PathVariable("id") Long id, HttpServletResponse response){
		Video answer = videos.findOne(id);
		if (answer == null){
			response.setStatus(404);	
		}
		return answer;
	}
	
	@RequestMapping(value="/video/{id}/like", method=RequestMethod.POST)
	public void like(@PathVariable("id") Long id, HttpServletResponse response, Principal p){
		logger.info("like user - {} movie - {}", p.getName(), id);
		Video video = videos.findOne(id);
		if ( video == null ){
			response.setStatus(404);	
		} else{
			Like like = new Like(p.getName());
			if ( !video.like(like) ){
				response.setStatus(400);
			}else{
				videos.save(video);
			}
		}
	}
	
	
	@RequestMapping(value="/video/{id}/unlike", method=RequestMethod.POST)
	public void unlike(@PathVariable("id") Long id, HttpServletResponse response, Principal p){
		logger.info("unlike user - {} movie - {}", p.getName(), id);
		Video video = videos.findOne(id);
		if ( video == null ){
			response.setStatus(404);	
		} else{
			if (video.unlike(new Like(p.getName()))){
				videos.save(video);
			}
		}		
	}	
	
	@RequestMapping(value="/video/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<? extends String> likedBy(@PathVariable("id") Long id){
		Video video = videos.findOne(id);
		return (Collection<? extends String>) (video != null ? video.getUnameLikes() : Collections.emptyList());
	}
	
	@RequestMapping(value="/video/search/findByName", method=RequestMethod.GET)
	public @ResponseBody Collection<? extends Video> findByName(@RequestParam("title") String name){
		return videos.findByName(name);
	}
	
	@RequestMapping(value="/video/search/findByDurationLessThan", method=RequestMethod.GET)
	public @ResponseBody Collection<? extends Video> findByDurationLessThan(@RequestParam("duration") Long duration){
		return videos.findByDurationLessThan(duration);
	}	
}
