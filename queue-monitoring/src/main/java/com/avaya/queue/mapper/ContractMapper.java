package com.avaya.queue.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.jdbc.core.RowMapper;

import com.avaya.queue.entity.CustomerContract;

public class ContractMapper implements RowMapper<CustomerContract> {

	public CustomerContract mapRow(ResultSet result, int rownum) throws SQLException {
		CustomerContract customerContract = new CustomerContract();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		
		customerContract.setRegion(result.getString("region"));
		customerContract.setCountry(result.getString("country"));
		customerContract.setStatus(result.getString("status"));
		customerContract.seteProject(result.getString("eProject"));
		customerContract.setSapContract(result.getString("sapContract"));
		customerContract.setFl(result.getString("fl"));
		customerContract.setSoldToName(result.getString("soldToName"));
		customerContract.setShipTo(result.getString("shipTo"));
		customerContract.setCustomerNameEndUser(result.getString("customerNameEndUser"));
		customerContract.setCommentsAppsSuppTeam(result.getString("commentsAppsSuppTeam"));
		customerContract.setSapOrder(result.getString("sapOrder"));
		
		if(result.getDate("startLastRenewed")!=null){
			DateTime dt = new DateTime(result.getDate("startLastRenewed"));
			customerContract.setStartLastRenewed(fmt.print(dt));
		}else{
			customerContract.setStartLastRenewed(null);
		}

		if(result.getDate("endContract")!=null){
			DateTime dt = new DateTime(result.getDate("endContract"));
			customerContract.setEndContract(fmt.print(dt));
		}else{
			customerContract.setEndContract(null);
		}
		
		customerContract.setSolutionApplication(result.getString("solutionApplication"));
		customerContract.setApsSuppMc(result.getString("apsSuppMc"));
		customerContract.setApsSuppDescription(result.getString("apsSuppDescription"));
		customerContract.setLinkToSapContractDoc(result.getString("linkToSapContractDoc"));
		customerContract.setManualDate(result.getString("manualDate"));
		
		return customerContract;
	}

}
