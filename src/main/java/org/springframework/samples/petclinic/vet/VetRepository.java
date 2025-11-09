/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository class for <code>Vet</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface VetRepository extends JpaRepository<Vet, Integer> {

	/**
	 * Retrieve all <code>Vet</code>s from the data store.
	 * @return a <code>List</code> of <code>Vet</code>s
	 */
	@Transactional(readOnly = true)
	@Cacheable("vets")
	@Override
	java.util.List<Vet> findAll();

	/**
	 * Retrieve all <code>Vet</code>s from data store in Pages
	 * @param pageable
	 * @return
	 * @throws DataAccessException
	 */
	@Transactional(readOnly = true)
	@Cacheable("vets")
	Page<Vet> findAll(Pageable pageable) throws DataAccessException;

	/**
	 * Search for vets by name and/or specialty.
	 * @param name partial name (first name or last name) to search for
	 * @param specialtyName specialty name to filter by
	 * @param pageable pagination information
	 * @return a Page of matching Vets
	 */
	@Transactional(readOnly = true)
	@Query("SELECT DISTINCT v FROM Vet v LEFT JOIN v.specialties s "
			+ "WHERE (:name IS NULL OR LOWER(v.firstName) LIKE LOWER(CONCAT('%', :name, '%')) "
			+ "OR LOWER(v.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) "
			+ "AND (:specialtyName IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :specialtyName, '%')))")
	Page<Vet> searchVets(@Param("name") String name, @Param("specialtyName") String specialtyName, Pageable pageable);

	/**
	 * Retrieve a Vet by id
	 * @param id the id to search for
	 * @return an Optional containing the Vet if found
	 */
	Optional<Vet> findById(Integer id);

}
