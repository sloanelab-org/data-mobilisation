package org.sloanelab.IntegratingReviewersAnnotations.model;

import java.util.Date;

public class ZooniAnnotatedData {
	
	String workflow_name,user_name,annotations,subject_data,subject_ids;
	Date created_at;
	
	public ZooniAnnotatedData (String workflow, String user, String annotations, String subjectData, String subjectIds, Date createdAt) {
		
		this.user_name = user;
		this.workflow_name=workflow;
		this.annotations = annotations;
		this.subject_data = subjectData;
		this.subject_ids = subjectIds;
		this.created_at = createdAt;
	}

	public String getWorkflow_name() {
		return workflow_name;
	}

	public void setWorkflow_name(String workflow_name) {
		this.workflow_name = workflow_name;
	}

	public String getSubject_ids() {
		return subject_ids;
	}

	public void setSubject_ids(String subject_ids) {
		this.subject_ids = subject_ids;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public String getAnnotations() {
		return annotations;
	}

	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

	public String getSubject_data() {
		return subject_data;
	}

	public void setSubject_data(String subject_data) {
		this.subject_data = subject_data;
	}


}
