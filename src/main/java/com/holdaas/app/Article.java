package com.holdaas.app;

public class Article {
	String head, body;
	public Article(String head, String body){
		this.head = head;
		this.body = body;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String toString(){
		return "Head: " + this.head + "\nBody: " + this.body;
	}
}
