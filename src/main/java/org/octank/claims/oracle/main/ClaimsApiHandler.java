package org.octank.claims.oracle.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.octank.claims.api.model.APIClaim;
import org.octank.claims.oracle.model.Claim;
import org.octank.claims.oracle.model.InsuranceCompany;
import org.octank.claims.oracle.model.MedicalProvider;
import org.octank.claims.oracle.model.Patient;
import org.octank.claims.oracle.model.Staff;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;

public class ClaimsApiHandler implements RequestStreamHandler {

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		// TODO Auto-generated method stub

	}

	
	public void saveClaim(
  		  InputStream inputStream, 
  		  OutputStream outputStream, 
  		  Context context)
  		  throws IOException {
  		 
  		    JSONParser parser = new JSONParser();
  		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
  		    JSONObject responseJson = new JSONObject();
  		     String claimNbr = null;
  		 
  		 
  		    try {
  		        JSONObject event = (JSONObject) parser.parse(reader);
  		 
  		        if (event.get("body") != null) {
  		            APIClaim claim = new APIClaim((String) event.get("body"));
  		            
  		            //call a method to map this to db model claim and save it
  		            
  		            System.out.println("begin parseNSaveClaim");
  		            
  		          claimNbr = parseNSaveClaim(claim);
  		            
  		 
  		        }
  		        
  		         
  		 
  		        JSONObject responseBody = new JSONObject();
  		        
  		      if(claimNbr != null)  {
  		        responseBody.put("message", "New Claim created with Claim # " +claimNbr);
  		        responseBody.put("claimId", claimNbr);
  		        
  		      }
  		      else  {
  		    	  responseBody.put("message", "Claim creation failed");
  		    	responseBody.put("claimId", "NA");
  		      }
  		 
  		        JSONObject headerJson = new JSONObject();
  		      headerJson.put("Access-Control-Allow-Origin", "*");
  		 
  		        responseJson.put("statusCode", 200);
  		        responseJson.put("headers", headerJson);
  		        responseJson.put("body", responseBody.toString());
  		 
  		    } catch (Exception pex) {
  		        responseJson.put("statusCode", 400);
  		        responseJson.put("exception", pex);
  		    }
  		 
  		    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
  		    writer.write(responseJson.toString());
  		    writer.close();
  		}
	
	
	public void listClaims(
	  		  InputStream inputStream, 
	  		  OutputStream outputStream, 
	  		  Context context)
	  		  throws IOException {
	  		 
	  		    JSONParser parser = new JSONParser();
	  		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	  		    JSONObject responseJson = new JSONObject();
	  		    String claimNbr = null;
	  		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	  		  List<APIClaim>  jsonClaims=null;
	  		 
	  		 
	  		    try {
	  		        
	  		    	System.out.println("before inputstream parse");
	  		    	JSONObject event = (JSONObject) parser.parse(reader);
	  		    	
	  		    	
	  		    	Date fromDate=null;
	  		    	Date toDate=null; 
	  		        
	  		 
	  		        if (event.get("queryStringParameters") != null) {
	  		        	System.out.println("before queryString parse");
	  		        	JSONObject qsp = (JSONObject) event.get("queryStringParameters");
    		            if (qsp.get("fromDate") != null) {
    		            	String sdate = (String)qsp.get("fromDate");
    		            	 fromDate = sdf.parse(sdate);
    		            }
    		            
    		            if (qsp.get("toDate") != null) {
    		            	String sdate = (String)qsp.get("toDate");
    		            	 toDate = sdf.parse(sdate);
    		            	
    		            }
	  		            
	  		            //call a method to list claims
    		            jsonClaims = retreiveClaims(fromDate,toDate);
	  		            
	  		 
	  		        }
	  		        
	  		         
	  		 
	  		        JSONObject responseBody = new JSONObject();
	  		        
	  		      if(jsonClaims != null)  {
	  		    	
	  		        responseBody.put("claims", jsonClaims);
	  		      }
	  		      else  {
	  		    	  responseBody.put("message", "No Claims found");
	  		      }
	  		      
	  		 
	  		        JSONObject headerJson = new JSONObject();
	  		        headerJson.put("Access-Control-Allow-Origin", "*");
	  		 
	  		        responseJson.put("statusCode", 200);
	  		        responseJson.put("headers", headerJson);
	  		        responseJson.put("body", responseBody.toString());
	  		 
	  		    } catch (Exception pex) {
	  		        responseJson.put("statusCode", 400);
	  		        responseJson.put("exception", pex);
	  		    }
	  		 
	  		    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
	  		    writer.write(responseJson.toString());
	  		    writer.close();
	  		}
	
	
	private List<APIClaim> retreiveClaims(Date from, Date to) {
		
		System.out.println("in retreiveClaims");
		
		
		List<APIClaim> acl = new ArrayList<APIClaim>();
		
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		 Session sessionObj = null;
		 List<String> jsonClaims=null;
		 
		 System.out.println("from:" +from);
		 System.out.println("to:" +to);
		 
		 try {
			 
			 sessionObj = sessionFactory.openSession();
			 
			 
			 String hql = "from Claim where updatedDate >= :from and updatedDate <= :to";
				
				
				Query query = sessionObj.createQuery(hql);
				
				
				
				query.setParameter("from", from);
				query.setParameter("to", to);
				
				
			
				List<Claim> lc = query.list();
				
				for (Claim c : lc) {
					
					System.out.println(c.getClaimId());
					
					APIClaim ac = new APIClaim();
					ac.setClaimId(c.getClaimId());
					ac.setAmountClaimed(c.getAmountClaimed().doubleValue());
					ac.setClaimStatus(c.getClaimStatus());
					ac.setMedicalCode(c.getMedicalCode());
					ac.setUpdatedDate(c.getUpdatedDate());
					ac.setInsurancePolicyNbr(c.getInsurancePolicyNbr());
					ac.setInsuranceCompanyId(c.getInsuranceCompany().getInsuranceCompanyName());
					ac.setGender(c.getPatient().getGender());
					ac.setPatientAddress(c.getPatient().getPatientAddress());
					ac.setPatientCity(c.getPatient().getPatientCity());
					ac.setPatientState(c.getPatient().getPatientState());
					ac.setPatientZip(c.getPatient().getPatientZip());
					ac.setPatientCountry(c.getPatient().getPatientCountry());
					ac.setPatientDateOfBirth(c.getPatient().getDateOfBirth());
					ac.setPatientName(c.getPatient().getPatientName());
					ac.setMedicalProviderId(c.getMedicalProvider().getMedicalProviderName());
					ac.setStaffId(c.getStaff().getStaffName());
					
					
					acl.add(ac);
					
				}
			 
				
			 
			 
		 } catch(Exception sqlException) {
				if(null != sessionObj.getTransaction()) {
					System.out.println("\n.......Transaction Is Being Rolled Back.......");
					sessionObj.getTransaction().rollback();
					
				}
				sqlException.printStackTrace();
			} finally {
				if(sessionObj != null) {
					sessionObj.close();
				}
				
				
			}
		
		
		
		
		return acl;
		
	}
	
	
	private String parseNSaveClaim(APIClaim apiClaim) {
		
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		 Session sessionObj = null;
		 String status="success";
		 String claimNbr="";
		
		
		try {
					sessionObj = sessionFactory.openSession();
					sessionObj.beginTransaction();
				
					Claim claim = new Claim();
					claim.setAmountClaimed(new BigDecimal(apiClaim.getAmountClaimed()));
					
					//gen claim id
					Query query = 
							sessionObj.createSQLQuery("select CLAIM_SEQ.nextval as num from dual")
					            .addScalar("num", StandardBasicTypes.BIG_INTEGER);
					String claimId = ((BigInteger) query.uniqueResult()).longValue() + "";
					claim.setClaimId(claimId);
					
					
					InsuranceCompany ic = new InsuranceCompany();
					ic.setInsuranceCompanyId(apiClaim.getInsuranceCompanyId());
					claim.setInsuranceCompany(ic);
					
					claim.setInsurancePolicyNbr(apiClaim.getInsurancePolicyNbr());
				
					
					MedicalProvider mp = new MedicalProvider();
					mp.setMedicalProviderId(apiClaim.getMedicalProviderId());
					claim.setMedicalProvider(mp);
					
					//find existing patient by name and zip, if not found then create it
					
					String patientId = setupPatient(sessionObj, apiClaim);
					
					Patient p = new Patient();
					p.setPatientId(patientId);
					
					claim.setPatient(p);
					
					Staff s = new Staff();
					
					s.setStaffId(apiClaim.getStaffId());
					claim.setStaff(s);
					
					claim.setClaimStatus(apiClaim.getClaimStatus());
					
					claim.setUpdatedDate(apiClaim.getUpdatedDate());
					claim.setMedicalCode(apiClaim.getMedicalCode());
					
					// determine policy nbr
					
					System.out.println("Insurnace Co Id-Gender:" + apiClaim.getInsuranceCompanyId() + "-" + apiClaim.getGender());
					
					if(apiClaim.getInsuranceCompanyId().equals("IC101")) {
						if(apiClaim.getGender().equals("Male")) {
							claim.setInsurancePolicyNbr("IC101-101");
						} else {
							claim.setInsurancePolicyNbr("IC101-102");
						}
					} else if(apiClaim.getInsuranceCompanyId().equals("IC102")) {
						if(apiClaim.getGender().equals("Male")) {
							claim.setInsurancePolicyNbr("IC102-103");
						} else {
							claim.setInsurancePolicyNbr("IC102-104");
						}
					}
					
					
					
						
					System.out.println("saving claim:" +claim.getClaimId());
						
					sessionObj.save(claim);
					
					claimNbr = claim.getClaimId();
					
					//sessionObj.flush();
			       // sessionObj.clear();
			        
			      //  System.out.println("saved claim:" +claim.getClaimId());
				
				// Committing The Transactions To The Database
				
				sessionObj.getTransaction().commit();
				
								
				
				
			} catch(Exception sqlException) {
				if(null != sessionObj.getTransaction()) {
					System.out.println("\n.......Transaction Is Being Rolled Back.......");
					sessionObj.getTransaction().rollback();
					status="failure";
				}
				sqlException.printStackTrace();
			} finally {
				if(sessionObj != null) {
					sessionObj.close();
				}
				
				
			}
		
		return claimNbr;
		
	}
	
	
	String setupPatient(Session session, APIClaim apiClaim)  {
		
		String hql = "from Patient where patientName = :patientName and patientZip = :patientZip";
		
		String patientName = apiClaim.getPatientName();
		String patientZip = apiClaim.getPatientZip();
		String patientId = null;
		Query query = session.createQuery(hql);
		
		System.out.println("PatientName:" + patientName);
		System.out.println("PatientZip:" + patientZip);
		
		query.setParameter("patientName", patientName);
		query.setParameter("patientZip", patientZip);
	
		List<Patient> lp = query.list();
		
		for (Patient p : lp) {
			patientId = p.getPatientId();
			System.out.println(p.getPatientId() +"-" +p.getPatientName());
		}
		 
		if(patientId == null)  {
			
			query = 
					session.createSQLQuery("select PATIENT_SEQ.nextval as num from dual")
			            .addScalar("num", StandardBasicTypes.BIG_INTEGER);
			
			patientId = ((BigInteger) query.uniqueResult()).longValue() + "";
			
			Patient p = new Patient();
			p.setPatientId(patientId);
			
			p.setDateOfBirth(apiClaim.getPatientDateOfBirth());
			p.setGender(apiClaim.getGender());
			p.setPatientAddress(apiClaim.getPatientAddress());
			p.setPatientCity(apiClaim.getPatientCity());
			p.setPatientCountry(apiClaim.getPatientCountry());
			p.setPatientName(apiClaim.getPatientName());
			p.setPatientState(apiClaim.getPatientState());
			p.setPatientZip(apiClaim.getPatientZip());
			System.out.println("saving patient");
			session.save(p);
			
		}
		
		
		return patientId;
		
		
		
	}
  
}
