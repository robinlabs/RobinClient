package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class FbFQLFeedPostUsersResponse  implements Serializable {
	@ML("data")
	protected FbFQLFeedPostUser users[]=null;
	
	/**
	 * @return the posts
	 */
	public FbFQLFeedPostUser[] getUsers() {
		return users;
	}

	public void setUsers(FbFQLFeedPostUser[] users) {
		this.users = users;
	}
}
