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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.ui.Model;

/**
 * Utility class for common domain entity mapping operations. Provides reusable methods
 * for entity lookup and pagination model building.
 *
 * @author Spring PetClinic Team
 */
public class DomainMapper {

	/**
	 * Finds an entity by its ID from the given repository.
	 * @param id the entity ID
	 * @param repository the repository to query
	 * @param entityName the entity name for error messages
	 * @param <T> the entity type
	 * @param <ID> the ID type
	 * @return the found entity
	 * @throws IllegalArgumentException if entity is not found
	 */
	public static <T, ID> T findEntityById(ID id, CrudRepository<T, ID> repository, String entityName) {
		Optional<T> optionalEntity = repository.findById(id);
		return optionalEntity.orElseThrow(() -> new IllegalArgumentException(
				entityName + " not found with id: " + id + ". Please ensure the ID is correct " + "and the "
						+ entityName.toLowerCase() + " exists in the database."));
	}

	/**
	 * Finds an entity by its ID from the given repository with a simpler error message.
	 * @param id the entity ID
	 * @param repository the repository to query
	 * @param entityName the entity name for error messages
	 * @param <T> the entity type
	 * @param <ID> the ID type
	 * @return the found entity
	 * @throws IllegalArgumentException if entity is not found
	 */
	public static <T, ID> T findEntityByIdSimple(ID id, CrudRepository<T, ID> repository, String entityName) {
		Optional<T> optionalEntity = repository.findById(id);
		return optionalEntity.orElseThrow(() -> new IllegalArgumentException(
				entityName + " not found with id: " + id + ". Please ensure the ID is correct "));
	}

	/**
	 * Adds pagination attributes to the model for a paginated result set.
	 * @param page the current page number (1-based)
	 * @param model the Spring MVC model
	 * @param paginated the paginated result
	 * @param attributeName the attribute name for the list (e.g., "listOwners",
	 * "listVets")
	 * @param viewName the view name to return
	 * @param <T> the entity type
	 * @return the view name
	 */
	public static <T> String addPaginationModel(int page, Model model, Page<T> paginated, String attributeName,
			String viewName) {
		List<T> content = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute(attributeName, content);
		return viewName;
	}

}
