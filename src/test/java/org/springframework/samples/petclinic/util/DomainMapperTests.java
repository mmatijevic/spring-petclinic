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
package org.springframework.samples.petclinic.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DomainMapper}.
 *
 * @author Spring PetClinic Team
 */
class DomainMapperTests {

	private CrudRepository<String, Integer> mockRepository;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		mockRepository = (CrudRepository<String, Integer>) mock(CrudRepository.class);
	}

	@Test
	void findEntityById_shouldReturnEntity_whenEntityExists() {
		// Given
		Integer id = 1;
		String entity = "TestEntity";
		when(mockRepository.findById(id)).thenReturn(Optional.of(entity));

		// When
		String result = DomainMapper.findEntityById(id, mockRepository, "TestEntity");

		// Then
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void findEntityById_shouldThrowException_whenEntityNotFound() {
		// Given
		Integer id = 999;
		when(mockRepository.findById(id)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> DomainMapper.findEntityById(id, mockRepository, "TestEntity"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("TestEntity not found with id: 999")
			.hasMessageContaining("Please ensure the ID is correct")
			.hasMessageContaining("and the testentity exists in the database");
	}

	@Test
	void findEntityByIdSimple_shouldReturnEntity_whenEntityExists() {
		// Given
		Integer id = 1;
		String entity = "TestEntity";
		when(mockRepository.findById(id)).thenReturn(Optional.of(entity));

		// When
		String result = DomainMapper.findEntityByIdSimple(id, mockRepository, "TestEntity");

		// Then
		assertThat(result).isEqualTo(entity);
	}

	@Test
	void findEntityByIdSimple_shouldThrowException_whenEntityNotFound() {
		// Given
		Integer id = 999;
		when(mockRepository.findById(id)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> DomainMapper.findEntityByIdSimple(id, mockRepository, "TestEntity"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("TestEntity not found with id: 999")
			.hasMessageContaining("Please ensure the ID is correct");
	}

	@Test
	void addPaginationModel_shouldAddAttributesToModel() {
		// Given
		int page = 2;
		List<String> content = Arrays.asList("Entity1", "Entity2", "Entity3");
		Page<String> paginatedData = new PageImpl<>(content);
		Model model = new ExtendedModelMap();

		// When
		String result = DomainMapper.addPaginationModel(page, model, paginatedData, "listEntities",
				"entities/entityList");

		// Then
		assertThat(result).isEqualTo("entities/entityList");
		assertThat(model.getAttribute("currentPage")).isEqualTo(2);
		assertThat(model.getAttribute("totalPages")).isEqualTo(1);
		assertThat(model.getAttribute("totalItems")).isEqualTo(3L);
		assertThat(model.getAttribute("listEntities")).isEqualTo(content);
	}

	@Test
	void addPaginationModel_shouldHandleEmptyPage() {
		// Given
		int page = 1;
		Page<String> emptyPage = new PageImpl<>(Arrays.asList());
		Model model = new ExtendedModelMap();

		// When
		String result = DomainMapper.addPaginationModel(page, model, emptyPage, "listEntities", "entities/entityList");

		// Then
		assertThat(result).isEqualTo("entities/entityList");
		assertThat(model.getAttribute("currentPage")).isEqualTo(1);
		assertThat(model.getAttribute("totalPages")).isEqualTo(1);
		assertThat(model.getAttribute("totalItems")).isEqualTo(0L);
		assertThat(model.getAttribute("listEntities")).asList().isEmpty();
	}

}
