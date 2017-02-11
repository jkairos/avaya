package com.avaya.queue.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.avaya.queue.entity.CustomerContract;
import com.avaya.queue.entity.SR;
import com.avaya.queue.mapper.ContractMapper;

@Repository
public class CustomerContractDaoImpl implements CustomerContractDao {
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private static final String SELECT_CLAUSE="SELECT region,"
				+ " country,"
				+ " status,"
				+ " eProject,"
				+ " sapContract ,"
				+ " fl ,"
				+ " soldToName ,"
				+ " shipTo ,"
				+ " customerNameEndUser,"
				+ " commentsAppsSuppTeam,"
				+ " sapOrder ,"
				+ " startLastRenewed,"
				+ " endContract,"
				+ " solutionApplication ,"
				+ " apsSuppMc ,"
				+ " apsSuppDescription ,"
				+ " linkToSapContractDoc FROM contracts ";
	@Autowired
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public List<CustomerContract> findByFl(String fl) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fl", fl);
		params.put("status", "Open");
		
		String sql=CustomerContractDaoImpl.SELECT_CLAUSE+ " WHERE fl=:fl and status=:status ";
		List<CustomerContract> contracts = null;
		try{
			contracts = namedParameterJdbcTemplate.query(sql, params, new ContractMapper());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return contracts;
	}

	public List<CustomerContract> findByName(SR sr) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "%"+sr.getAccount()+"%");
		if(sr.getParentName()!=null && !sr.getParentName().equals("")){
			params.put("parentName", "%"+sr.getParentName()+"%");
		}
		
		params.put("status", "Open");
		
		String sql=CustomerContractDaoImpl.SELECT_CLAUSE+ " WHERE ((soldToName like :name or customerNameEndUser like :name) ";
		
		if(sr.getParentName()!=null && !sr.getParentName().equals("")){
			sql+=" or (soldToName like :parentName or customerNameEndUser like :parentName) ";
		}
		
		sql+=" or (commentsAppsSuppTeam like :name)) and status=:status ";
		
		List<CustomerContract> contracts = namedParameterJdbcTemplate.query(sql, params, new ContractMapper());
		
		return contracts;
	}

}
