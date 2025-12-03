package com.repository;





import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.FileDB;



@Repository
public interface FileRepository extends JpaRepository<FileDB, Integer>{
	
	FileDB findByPersonId(int id);
	
	void deleteByPersonId(Integer id);
}
