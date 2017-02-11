package com.avaya.queue.dao;

import java.util.List;

import com.avaya.queue.entity.CustomerContract;
import com.avaya.queue.entity.SR;

public interface CustomerContractDao {
	List<CustomerContract> findByFl(String fl);
	List<CustomerContract> findByName(SR sr);
}
