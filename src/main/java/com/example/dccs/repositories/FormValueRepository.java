package com.example.dccs.repositories;

import com.example.dccs.models.FormValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormValueRepository extends JpaRepository<FormValue, FormValue.Id> {
}
