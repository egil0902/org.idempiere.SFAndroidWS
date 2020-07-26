/**
 * @author carlos
 * @date 01/05/2012
 * @time 23:07:20
 * @type MAppDroidServicesImpl
 */
package org.mb.appdroid;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Login;

import org.idempiere.adinterface.CompiereService;
import com.erpconsultoresasociados.DataSetSQLS;
import com.erpconsultoresasociados.InitialLoad;
import com.erpconsultoresasociados.InitialLoadDocument;
import com.erpconsultoresasociados.InitialLoadResponse;
import com.erpconsultoresasociados.InitialLoadResponseDocument;

/**
 * @author carlos
 *
 */
public class MAppDroidServicesImpl {
	
	/**
	 * 
	 */
	public MAppDroidServicesImpl() {
		// TODO Auto-generated constructor stub
		m_adempiere = new CompiereService();
		m_adempiere.connect();	
	}
	
	public InitialLoadResponseDocument initialLoad() throws SQLException
	{
		InitialLoadResponseDocument resp  = InitialLoadResponseDocument.Factory.newInstance();
		
		StringBuffer sql = new StringBuffer();
		sql.append("select XXIL.*,TAB.TableName from XX_MB_InitialLoad XXIL "
				+ "Left Join AD_Table TAB on XXIL.AD_Table_ID=TAB.AD_Table_ID "
				+ "Where XXIL.AD_Client_ID ="+m_AD_Client_ID+" And "
				+ "XXIL.isActive='Y' order by XXIL.SeqNo ");
		
		PreparedStatement ps = DB.prepareStatement(sql.toString());
		ResultSet rs = ps.executeQuery();
		
		DataSetSQLS dataset =resp.addNewInitialLoadResponse();
		while (rs.next())
		{
			if (rs.getString("tablename")==null)
			{
				InitialLoadResponse rp = dataset.addNewInitialLoadResponse();
				rp.setSql(rs.getString("sql"));
				rp.setName(rs.getString("name"));
			}
			else
				loadFromTable(dataset,rs);
			
		}
		rs.close();
		ps.close();
		return resp;
		
	}
	private void loadFromTable(DataSetSQLS ds, ResultSet rs) throws SQLException
	{
		String l_campo = "";
		int l_init=-1;
		int l_end=-1;
		String l_sql = "";
		Object l_Value = new Object();
		StringBuffer sql = new StringBuffer(),where = new StringBuffer();
		
		//red1 check for empty not null whereClause causing ' WHERE ' string.
		if(rs.getString("whereClause") == null || rs.getString("whereClause").equals(""))
			where.append("");
		else
			where.append(" WHERE " + rs.getString("whereclause").replaceAll("%AD_User_ID%", Env.getContext(m_adempiere.getCtx(), "#AD_User_ID")));
			//where.append(rs.getString("whereclause")!=""?" Where " + rs.getString("whereclause").replaceAll("%AD_User_ID%", "100"):"");//Env.getContext(m_adempiere.getCtx(), "#AD_User_ID")
		where.append((where.length()>0?" AND (AD_Client_ID="+m_AD_Client_ID+" Or AD_Client_ID=0)":" WHERE (AD_Client_ID="+m_AD_Client_ID+" OR AD_Client_ID=0)"));
		
		sql.append("SELECT * FROM "+rs.getString("tablename")+where.toString());
		
		//Preparing the Consultation
		PreparedStatement psquery = DB.prepareStatement(sql.toString());
		ResultSet rsquery =psquery.executeQuery();
		
		while(rsquery.next())
		{
			l_sql = rs.getString("sql");
			while (l_sql.indexOf("$")>0)
			{
				l_init=l_sql.indexOf("$");
				l_end=l_sql.indexOf("$",l_init+1);
				l_campo = l_sql.substring(l_init+1,l_end);
				l_Value =transformValue(rsquery.getObject(l_campo));
				l_sql = l_sql.substring(0,l_init ) + l_Value+ l_sql.substring(l_end+1,l_sql.length());
			}
			InitialLoadResponse rp = ds.addNewInitialLoadResponse();
			rp.setSql(l_sql);
			rp.setName(rs.getString("name"));
			
		}
		rsquery.close();
		psquery.close();
		
	}
	
	private String transformValue(Object p_value)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		
		if(p_value==null)
			p_value = new String ("null");
		else
		{
			if (p_value instanceof String)
				p_value = "'"+p_value+"'";
			else if (p_value instanceof BigDecimal)
				p_value = p_value.toString();
			else if (p_value instanceof Integer)
				p_value = p_value.toString();
			else if (p_value instanceof Date)
				p_value="'"+dateFormat.format((Date)p_value)+"'";
			else if (p_value instanceof java.util.Date)
				p_value="'"+dateFormat.format((java.util.Date)p_value)+"'";
			else if (p_value instanceof Timestamp)
				p_value = "'"+dateFormat.format((Timestamp)p_value)+"'";
			else if (p_value instanceof Double)
				p_value = p_value.toString();
		}
		return p_value.toString();	
	}
	protected boolean validateuser(InitialLoadDocument input)
	{
		log.warning("AQUITOY");
		InitialLoad il = input.getInitialLoad();
		return loggin(il.getUser(), il.getPass());
	}
	
	/*
	 * @author Carlos Parada
	 * @date 01/05/2012
	 * @time 14:14:10
	 * @type AppDroidServices
	 * @param user Username & Password
	 * @description
	 * @return returns boolean depending if the user exists and has roles assigned
	 * performs user login with associated role in adempiere 
	 */
	private boolean loggin(String user, String pass)
	{
		boolean m_loggin =false; 
		Login loggin  = new Login(m_adempiere.getCtx()); 
		KeyNamePair[] users =	loggin.getRoles (user, pass);
		if (users!=null)
		{
			if(users.length>0)
			{
				m_loggin = true;
				KeyNamePair[] clients = loggin.getClients (users[0]);
				if (clients != null)
					m_AD_Client_ID = (clients.length > 0?(Integer)clients[0].getKey():null);
			}
			else
				m_loggin= false;
		}
		return m_loggin;
	}
	
	protected CompiereService m_adempiere;
	private Integer m_AD_Client_ID = 11;
	protected static CLogger	log = CLogger.getCLogger(AppDroidServicesImpl.class);
}
