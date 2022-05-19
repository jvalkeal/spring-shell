/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.standard.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.shell.command.CommandRegistration;
import org.springframework.util.StringUtils;

/**
 * Model encapsulating info about command structure which is more
 * friendly to show and render via templating.
 *
 * @author Janne Valkealahti
 */
public class GroupsInfoModel {

	private boolean showGroups = true;
	private final List<GroupCommandInfoModel> groups;

	GroupsInfoModel(boolean showGroups, List<GroupCommandInfoModel> groups) {
		this.showGroups = showGroups;
		this.groups = groups;
	}

	/**
	 * Builds {@link GroupsInfoModel} from command registrations.
	 *
	 * @param showGroups the flag showing groups
	 * @param registrations the command registrations
	 * @return a groups info model
	 */
	public static GroupsInfoModel of(boolean showGroups, Map<String, CommandRegistration> registrations) {
		// collect commands into groups with sorting
		SortedMap<String, Map<String, CommandRegistration>> commandsByGroupAndName = registrations.entrySet().stream()
			.collect(Collectors.groupingBy(
				e -> StringUtils.hasText(e.getValue().getGroup()) ? e.getValue().getGroup() : "",
				TreeMap::new,
				Collectors.toMap(Entry::getKey, Entry::getValue)
			));

		// build model
		List<GroupCommandInfoModel> gcims = commandsByGroupAndName.entrySet().stream()
			.map(e -> {
				List<CommandInfoModel> cims = e.getValue().values().stream()
					.map(CommandInfoModel::of)
					.collect(Collectors.toList());
				GroupCommandInfoModel groupCommandInfoModel = new GroupCommandInfoModel(e.getKey(), cims);
				return groupCommandInfoModel;
			})
			.collect(Collectors.toList());
		return new GroupsInfoModel(showGroups, gcims);
	}

	public boolean getShowGroups() {
		return this.showGroups;
	}

	public List<GroupCommandInfoModel> getGroups() {
		return this.groups;
	}
}
