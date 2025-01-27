package com.ewch.springboot.reactor.app.model;

public class UserComment {

	private User user;
	private Comment comment;

	public UserComment(User user, Comment comment) {
		this.user = user;
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "UserComment{" +
			"user=" + user +
			", comment=" + comment +
			'}';
	}
}
