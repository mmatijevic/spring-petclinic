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
package org.springframework.samples.petclinic.system;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper class for common form binding operations.
 * Provides utility methods to bind collections from request parameter IDs.
 */
public class FormBindingHelper {

	/**
	 * Binds a collection of entities to a target object based on a list of IDs.
	 * This method clears the existing collection, fetches all available entities,
	 * and adds only those whose IDs are in the selected list.
	 *
	 * @param <T> The type of entity being bound
	 * @param selectedIds The list of selected entity IDs from the form
	 * @param allEntities The complete list of available entities
	 * @param clearAction Action to clear the existing collection on the target object
	 * @param addAction Action to add an entity to the target object's collection
	 * @param idExtractor Function to extract the ID from an entity
	 */
	public static <T> void bindCollectionFromIds(
			List<Integer> selectedIds,
			List<T> allEntities,
			Runnable clearAction,
			Consumer<T> addAction,
			Function<T, Integer> idExtractor) {

		clearAction.run();

		if (selectedIds != null && !selectedIds.isEmpty()) {
			Set<Integer> selectedIdSet = new HashSet<>(selectedIds);
			for (T entity : allEntities) {
				if (selectedIdSet.contains(idExtractor.apply(entity))) {
					addAction.accept(entity);
				}
			}
		}
	}

}
