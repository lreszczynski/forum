package com.example.demo.threads;

import com.example.demo.posts.Post;

public interface TestSimpleProj {
	Thread getThread();
	
	Post getLastPost();
	
	Long getPostsCount();
	
	/*private boolean active;
	
	private User user;*//*
	
	@JsonProperty("posts-count")
	private int postsCount;
	
	@JsonProperty("last-post")
	private Post lastPost;*/
}
