package com.feedss.contact.qcloud.domain;

import lombok.Data;

@Data
public class ImportUserRequestBody {

	private String Identifier;
	private String Nick;
	private String FaceUrl;
	
	public ImportUserRequestBody(String Identifier, String nickname, String faceUrl){
		this.Identifier = Identifier;
		this.Nick = nickname;
		this.FaceUrl = faceUrl;
	}
}
