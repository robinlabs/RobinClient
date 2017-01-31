package com.magnifis.parking.model;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.pref.PasswordPreference;

import static com.magnifis.parking.utils.Utils.*;

import android.accounts.Account;
import android.content.SharedPreferences;

public class MailService {
    protected String imapServer=null, mailAddress=null,
    		passwordPrefKey=null, passwordDialogTitle=null;
    
    protected boolean suppportsSearchById=true;
    
    
    public boolean isSuppportsSearchById() {
		return suppportsSearchById;
	}

	public void setSuppportsSearchById(boolean suppportsSearchById) {
		this.suppportsSearchById = suppportsSearchById;
	}

	public String getPasswordDialogTitle() {
		return passwordDialogTitle;
	}

	public void setPasswordDialogTitle(String passwordDialogTitle) {
		this.passwordDialogTitle = passwordDialogTitle;
	}

	public String getPasswordPrefKey() {
		return passwordPrefKey;
	}

	public void setPasswordPrefKey(String passwordPrefKey) {
		this.passwordPrefKey = passwordPrefKey;
	}

	public String getImapServer() {
		return imapServer;
	}

	public void setImapServer(String imapServer) {
		this.imapServer = imapServer;
	}

	public String getMailAddress() {
		return mailAddress;
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}
	
	public MailService() {}
	
	
	public static MailService fromGoogleAccount(Account ac) {
		MailService ms=new MailService();
		ms.imapServer="imap.googlemail.com";
		ms.mailAddress=ac.name;		
		ms.passwordPrefKey="gmailPassword";
		ms.passwordDialogTitle=App.self.getString(R.string.gmail_password);
		ms.mailType=App.self.MT_GMAIL;
		return ms;
	}
	
	protected Integer mailType=null;
	
	public Integer getMailType() {
		return mailType;
	}

	public void setMailType(Integer mailType) {
		this.mailType = mailType;
	}
	
	public boolean isYahoo() {
		return mailType!=null&&mailType==App.self.MT_YAHOO;
	}
	
	public boolean isGmail() {
		return mailType!=null&&mailType==App.self.MT_GMAIL;
	}

	public static MailService fromPreferences() {
		int mt= App.self.getMailAccountType();
		
		if (mt==App.self.MT_GMAIL) {
			Account ac=App.self.getGmailAccount();
			return ac==null?null:fromGoogleAccount(ac);
		} else 
		if (mt==App.self.MT_YAHOO){
			String domain=trim(App.self.getStringPref(R.string.PfYahooDomain)),
					 mbox=trim(App.self.getStringPref(R.string.PfYahooMailBox));
			if (!(isEmpty(domain)||isEmpty(mbox))) {
				MailService ms=new MailService();
				ms.imapServer="imap.mail.yahoo.com";
				ms.suppportsSearchById=false;
				ms.mailAddress=mbox+'@'+domain;		
				ms.passwordPrefKey="yahooPassword";
				ms.passwordDialogTitle=App.self.getString(R.string.yahoo_password);
				ms.mailType=App.self.MT_YAHOO;
				return ms;
			}
		}
		
	
		return null;
	}
}
