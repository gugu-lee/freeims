package org.freeims.ims.config;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProperties extends Properties
{

	private static final long serialVersionUID = -6489794304188916793L;

	public void addProperty(UserProperty up)
	{
		this.setProperty(up.getName(), up.getValue());
	}
	
//	public void addProperty(String k ,String value)
//	{
//		props.setProperty(k, value);
//	}

	public  String replaceUserProperty(String s)
	{
		if (s == null)
		{
			return s;
		}
		//String s = "pcscf.${f._}-icscf.${f}";
		Pattern p = Pattern.compile("\\$\\{[a-zA-Z_0-9\\.]+\\}");
		 Matcher m = p.matcher(s);
		 boolean b = m.find();
		 String k,pv;
		 while (b)
		 {
//			 System.out.println(m.start());
//			 System.out.println(m.end());
			 k=s.substring(m.start(),m.end());
			 if ((pv=this.getProperty(k))!=null)
			 {
				 s=s.replace(s.subSequence(m.start(),m.end()),pv);
			 }
			 m = p.matcher(s);
			 b = m.find();
		 }
		 return s;
	
	}
}
