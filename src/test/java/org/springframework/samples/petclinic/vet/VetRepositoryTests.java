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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the VetRepository search functionality.
 *
 * @author Cursor
 */
@DataJpaTest
class VetRepositoryTests {

	@Autowired
	private VetRepository vetRepository;

	@Test
	void testSearchVetsByFirstName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("James", null, pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		assertThat(vets.getContent().get(0).getFirstName()).isEqualTo("James");
	}

	@Test
	void testSearchVetsByLastName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("Carter", null, pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		assertThat(vets.getContent().get(0).getLastName()).isEqualTo("Carter");
	}

	@Test
	void testSearchVetsByPartialName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("lea", null, pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		// Should match "Leary"
		assertThat(vets.getContent()).anyMatch(vet -> vet.getLastName().toLowerCase().contains("lea"));
	}

	@Test
	void testSearchVetsBySpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets(null, "radiology", pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		assertThat(vets.getContent()).allMatch(vet -> vet.getSpecialties()
			.stream()
			.anyMatch(specialty -> specialty.getName().toLowerCase().contains("radiology")));
	}

	@Test
	void testSearchVetsByPartialSpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets(null, "dent", pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		// Should match "dentistry"
		assertThat(vets.getContent()).allMatch(vet -> vet.getSpecialties()
			.stream()
			.anyMatch(specialty -> specialty.getName().toLowerCase().contains("dent")));
	}

	@Test
	void testSearchVetsByNameAndSpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("Helen", "radiology", pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isNotEmpty();
		assertThat(vets.getContent().get(0).getFirstName()).isEqualTo("Helen");
		assertThat(vets.getContent()).allMatch(vet -> vet.getSpecialties()
			.stream()
			.anyMatch(specialty -> specialty.getName().toLowerCase().contains("radiology")));
	}

	@Test
	void testSearchVetsWithNoCriteria() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets(null, null, pageable);

		assertThat(vets).isNotNull();
		// Should return all vets (at least the ones loaded in data.sql)
		assertThat(vets.getContent().size()).isGreaterThan(0);
	}

	@Test
	void testSearchVetsWithEmptyStrings() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("", "", pageable);

		assertThat(vets).isNotNull();
		// Should return all vets when search criteria are empty strings
		assertThat(vets.getContent().size()).isGreaterThan(0);
	}

	@Test
	void testSearchVetsWithNonexistentName() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets("Nonexistent", null, pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isEmpty();
	}

	@Test
	void testSearchVetsWithNonexistentSpecialty() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vets = vetRepository.searchVets(null, "nonexistent", pageable);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).isEmpty();
	}

	@Test
	void testSearchVetsCaseInsensitive() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Vet> vetsUpperCase = vetRepository.searchVets("JAMES", null, pageable);
		Page<Vet> vetsLowerCase = vetRepository.searchVets("james", null, pageable);

		assertThat(vetsUpperCase.getContent()).isNotEmpty();
		assertThat(vetsLowerCase.getContent()).isNotEmpty();
		assertThat(vetsUpperCase.getContent()).hasSize(vetsLowerCase.getContent().size());
	}

	@Test
	void testSearchVetsWithPagination() {
		Pageable firstPage = PageRequest.of(0, 2);
		Page<Vet> vets = vetRepository.searchVets(null, null, firstPage);

		assertThat(vets).isNotNull();
		assertThat(vets.getContent()).hasSizeLessThanOrEqualTo(2);
		assertThat(vets.getTotalElements()).isGreaterThanOrEqualTo(vets.getContent().size());
	}

}
