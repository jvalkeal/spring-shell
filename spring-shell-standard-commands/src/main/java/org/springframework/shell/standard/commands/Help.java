/*
 * Copyright 2017-2022 the original author or authors.
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.shell.Availability;
import org.springframework.shell.ParameterDescription;
import org.springframework.shell.Utils;
import org.springframework.shell.command.CommandOption;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.CommandValueProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;

/**
 * A command to display help about all available commands.
 *
 * @author Eric Bottard
 * @author Janne Valkealahti
 */
@ShellComponent
public class Help extends AbstractShellComponent {

	/**
	 * Marker interface for beans providing {@literal help} functionality to the shell.
	 *
	 * <p>
	 * To override the help command, simply register your own bean implementing that interface
	 * and the standard implementation will back off.
	 * </p>
	 *
	 * <p>
	 * To disable the {@literal help} command entirely, set the
	 * {@literal spring.shell.command.help.enabled=false} property in the environment.
	 * </p>
	 *
	 * @author Eric Bottard
	 */
	public interface Command {
	}

	private MessageInterpolator messageInterpolator = Utils.defaultValidatorFactory().getMessageInterpolator();
	private boolean showGroups = true;
	private TemplateExecutor templateExecutor;
	private String commandTemplate;
	private String commandsTemplate;


	public Help(TemplateExecutor templateExecutor) {
		this.templateExecutor = templateExecutor;
	}

	@Autowired(required = false)
	public void setValidatorFactory(ValidatorFactory validatorFactory) {
		this.messageInterpolator = validatorFactory.getMessageInterpolator();
	}

	@ShellMethod(value = "Display help about available commands.", prefix = "-")
	public CharSequence help(
			@ShellOption(defaultValue = ShellOption.NULL, valueProvider = CommandValueProvider.class, value = { "-C",
					"--command" }, help = "The command to obtain help for.", arity = Integer.MAX_VALUE) String command)
			throws IOException {
		if (command == null) {
			return listCommands();
		}
		else {
			return documentCommand(command);
		}

	}

	@ShellMethod(value = "Display help about available commands")
	public AttributedString helpx(
			@ShellOption(defaultValue = ShellOption.NULL, valueProvider = CommandValueProvider.class, value = { "-C",
					"--command" }, help = "The command to obtain help for.", arity = Integer.MAX_VALUE) String command)
			throws IOException {
		if (command == null) {
			return renderCommands();
		}
		else {
			return renderCommand(command);
		}
	}

	/**
	 * Sets a location for a template rendering command help.
	 *
	 * @param commandTemplate the command template location
	 */
	public void setCommandTemplate(String commandTemplate) {
		this.commandTemplate = commandTemplate;
	}

	/**
	 * Sets a location for a template rendering commands help.
	 *
	 * @param commandsTemplate the commands template location
	 */
	public void setCommandsTemplate(String commandsTemplate) {
		this.commandsTemplate = commandsTemplate;
	}

	/**
	 * Sets if groups should be shown in a listing, defaults to true. If not enabled
	 * a simple list is shown without groups.
	 *
	 * @param showGroups the flag to show groups
	 */
	public void setShowGroups(boolean showGroups) {
		this.showGroups = showGroups;
	}

	private AttributedString renderCommands() {
		Map<String, CommandRegistration> registrations = getCommandCatalog().getRegistrations();

		Map<String, Object> model;
		boolean isStg = this.commandTemplate.endsWith(".stg");
		if (isStg) {
			model = new HashMap<>();
			model.put("model", buildCommandsModel(registrations));
		}
		else {
			model = buildCommandsModel(registrations);
		}

		String templateResource = resourceAsString(getResourceLoader().getResource(this.commandsTemplate));
		return isStg ? this.templateExecutor.renderGroup(templateResource, model) : this.templateExecutor.render(templateResource, model);
	}

	private AttributedString renderCommand(String command) {
		Map<String, CommandRegistration> registrations = getCommandCatalog().getRegistrations();
		CommandRegistration registration = registrations.get(command);
		if (registration == null) {
			throw new IllegalArgumentException("Unknown command '" + command + "'");
		}

		Map<String, Object> model;
		boolean isStg = this.commandTemplate.endsWith(".stg");
		if (isStg) {
			model = new HashMap<>();
			model.put("model", buildCommandModel(registration));
		}
		else {
			model = buildCommandModel(registration);
		}

		String templateResource = resourceAsString(getResourceLoader().getResource(this.commandTemplate));
		return isStg ? this.templateExecutor.renderGroup(templateResource, model) : this.templateExecutor.render(templateResource, model);
	}

	private Map<String, Object> buildCommandsModel(Map<String, CommandRegistration> registrations) {
		Map<String, Object> model = new HashMap<>();

		model.put("showGroups", this.showGroups);

		SortedMap<String, Map<String, CommandRegistration>> commandsByGroupAndName = registrations.entrySet().stream()
			.collect(Collectors.groupingBy(
				e -> StringUtils.hasText(e.getValue().getGroup()) ? e.getValue().getGroup() : "",
				TreeMap::new,
				Collectors.toMap(Entry::getKey, Entry::getValue)
			));
		List<CommandGroupModel> commandGroupsModels =  commandsByGroupAndName.entrySet().stream()
			.map(e -> {
				CommandGroupModel commandGroupsModel = new CommandGroupModel();
				commandGroupsModel.setGroup(e.getKey());
				commandGroupsModel.setRegistrations(new ArrayList<>(e.getValue().values()));
				return commandGroupsModel;
			})
			.collect(Collectors.toList());
		model.put("groups", commandGroupsModels);
		model.put("registrations", registrations.values());

		return model;
	}

	@SuppressWarnings("unused")
	private static class CommandGroupModel {
		String group;
		List<CommandRegistration> registrations;

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public List<CommandRegistration> getRegistrations() {
			return registrations;
		}

		public void setRegistrations(List<CommandRegistration> registrations) {
			this.registrations = registrations;
		}
	}

	private Map<String, Object> buildCommandModel(CommandRegistration registration) {
		Map<String, Object> model = new HashMap<>();

		model.put("commandName", registration.getCommand());
		model.put("commandShortDesc", registration.getDescription());

		List<Map<String, Object>> optionsModel = new ArrayList<>();
		registration.getOptions().forEach(o -> {
			Map<String, Object> optionModel = new HashMap<>();
			optionModel.put("longNames", o.getLongNames());
			optionModel.put("shortNames", o.getShortNames());
			List<String> arguments = Stream.concat(Stream.of(o.getLongNames()).map(a -> "--" + a), Stream.of(o.getShortNames()).map(s -> "-" + s)).collect(Collectors.toList());
			optionModel.put("arguments", arguments);
			String type = o.getType() == null ? "String" : ClassUtils.getShortName(o.getType().getRawClass());
			optionModel.put("type", type);
			optionsModel.add(optionModel);
		});
		model.put("options", optionsModel);

		return model;
	}

	private static String resourceAsString(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Return a description of a specific command. Uses a layout inspired by *nix man pages.
	 */
	private CharSequence documentCommand(String command) {
		Map<String, CommandRegistration> registrations = getCommandCatalog().getRegistrations();
		CommandRegistration registration = registrations.get(command);
		if (registration == null) {
			throw new IllegalArgumentException("Unknown command '" + command + "'");
		}

		AttributedStringBuilder result = new AttributedStringBuilder().append("\n\n");
		List<ParameterDescription> parameterDescriptions = getParameterDescriptions(registration);

		// NAME
		documentCommandName(result, command, registration.getDescription());

		// SYNOPSYS
		documentSynopsys(result, command, parameterDescriptions);

		// OPTIONS
		documentOptions(result, parameterDescriptions);

		// ALSO KNOWN AS
		documentAliases(result, command, registrations, registration);

		// AVAILABILITY
		documentAvailability(result, registration);

		result.append("\n");
		return result;
	}

	private void documentCommandName(AttributedStringBuilder result, String command, String help) {
		result.append("NAME", AttributedStyle.BOLD).append("\n\t");
		result.append(command).append(" - ").append(help).append("\n\n");
	}

	private void documentSynopsys(AttributedStringBuilder result, String command,
			List<ParameterDescription> parameterDescriptions) {
		result.append("SYNOPSYS", AttributedStyle.BOLD).append("\n\t");
		result.append(command, AttributedStyle.BOLD);
		result.append(" ");

		for (ParameterDescription description : parameterDescriptions) {

			if (description.defaultValue().isPresent() && description.formal().length() > 0) {
				result.append("["); // Whole parameter is optional, as there is a default value (1)
			}
			List<String> keys = description.keys();
			if (!keys.isEmpty()) {
				if (!description.mandatoryKey()) {
					result.append("["); // Specifying a key is optional (ie positional params). (2)
				}
				result.append(first(keys), AttributedStyle.BOLD);
				if (!description.mandatoryKey()) {
					result.append("]"); // (close 2)
				}
				if (!description.formal().isEmpty()) {
					result.append(" ");
				}
			}
			if (description.defaultValueWhenFlag().isPresent()) {
				result.append("["); // Parameter can be used as a toggle flag (3)
			}
			appendUnderlinedFormal(result, description);
			if (description.defaultValueWhenFlag().isPresent()) {
				result.append("]"); // (close 3)
			}
			if (description.defaultValue().isPresent() && description.formal().length() > 0) {
				result.append("]"); // (close 1)
			}
			result.append("  "); // two spaces between each param for better legibility
		}
		result.append("\n\n");
	}

	private void documentOptions(AttributedStringBuilder result, List<ParameterDescription> parameterDescriptions) {
		if (!parameterDescriptions.isEmpty()) {
			result.append("OPTIONS", AttributedStyle.BOLD).append("\n");
		}
		for (ParameterDescription description : parameterDescriptions) {
			result.append("\t").append(description.keys().stream().collect(Collectors.joining(" or ")),
					AttributedStyle.BOLD);
			if (description.formal().length() > 0) {
				if (!description.keys().isEmpty()) {
					result.append("  ");
				}
				description.defaultValueWhenFlag().ifPresent(f -> result.append('['));
				appendUnderlinedFormal(result, description);
				description.defaultValueWhenFlag().ifPresent(f -> result.append(']'));
				result.append("\n\t");
			}
			else if (description.keys().size() > 1) {
				result.append("\n\t");
			}
			result.append("\t");
			result.append(description.help()).append('\n');
			// Optional parameter
			if (description.defaultValue().isPresent()) {
				result
						.append("\t\t[Optional, default = ", AttributedStyle.BOLD)
						.append(description.defaultValue().get(), AttributedStyle.BOLD.italic());
				description.defaultValueWhenFlag().ifPresent(
						s -> result.append(", or ", AttributedStyle.BOLD)
								.append(s, AttributedStyle.BOLD.italic())
								.append(" if used as a flag", AttributedStyle.BOLD));

				result.append("]", AttributedStyle.BOLD);
			} // Mandatory parameter, but with a default when used as a flag
			else if (description.defaultValueWhenFlag().isPresent()) {
				result
						.append("\t\t[Mandatory, default = ", AttributedStyle.BOLD)
						.append(description.defaultValueWhenFlag().get(), AttributedStyle.BOLD.italic())
						.append(" when used as a flag]", AttributedStyle.BOLD);
			} // true mandatory parameter
			else {
				result.append("\t\t[Mandatory]", AttributedStyle.BOLD);
			}
			result.append('\n');
			if (description.elementDescriptor() != null) {
				for (ConstraintDescriptor<?> constraintDescriptor : description.elementDescriptor()
						.getConstraintDescriptors()) {
					String friendlyConstraint = messageInterpolator.interpolate(
							constraintDescriptor.getMessageTemplate(), new DummyContext(constraintDescriptor));
					result.append("\t\t[" + friendlyConstraint + "]\n", AttributedStyle.BOLD);
				}
			}
			result.append('\n');
		}
	}

	private void documentAliases(AttributedStringBuilder result, String command,
			Map<String, CommandRegistration> registrations, CommandRegistration registration) {
		List<String> aliases = registrations.entrySet().stream()
			.filter(e -> e.getValue().equals(registration))
			.map(Map.Entry::getKey)
			.filter(c -> !command.equals(c))
			.collect(Collectors.toList());

		if (!aliases.isEmpty()) {
			result.append("ALSO KNOWN AS", AttributedStyle.BOLD).append("\n");
			for (String alias : aliases) {
				result.append('\t').append(alias).append('\n');
			}
		}
	}

	private void documentAvailability(AttributedStringBuilder result, CommandRegistration registration) {
		Availability availability = registration.getAvailability();
		if (!availability.isAvailable()) {
			result.append("CURRENTLY UNAVAILABLE", AttributedStyle.BOLD).append("\n");
			result.append('\t').append("This command is currently not available because ")
					.append(availability.getReason())
					.append(".\n");
		}
	}

	private String first(List<String> keys) {
		return keys.iterator().next();
	}

	private CharSequence listCommands() {
		AttributedStringBuilder result = new AttributedStringBuilder();
		result.append("AVAILABLE COMMANDS\n\n", AttributedStyle.BOLD);

		SortedMap<String, Map<String, CommandRegistration>> commandsByGroupAndName = getCommandCatalog().getRegistrations().entrySet().stream()
			.collect(Collectors.groupingBy(
				e -> StringUtils.hasText(e.getValue().getGroup()) ? e.getValue().getGroup() : "",
				TreeMap::new,
				Collectors.toMap(Entry::getKey, Entry::getValue)
			));

		commandsByGroupAndName.forEach((group, commandsInGroup) -> {
			if (showGroups) {
				result.append("".equals(group) ? "Default" : group, AttributedStyle.BOLD).append('\n');
			}
			Map<CommandRegistration, SortedSet<String>> commandNamesByMethod = commandsInGroup.entrySet().stream()
					.collect(groupingBy(Entry::getValue, // group by command method
							mapping(Entry::getKey, toCollection(TreeSet::new)))); // sort command names
			// display commands, sorted alphabetically by their first alias
			commandNamesByMethod.entrySet().stream().sorted(sortByFirstCommandName()).forEach(e -> {
				String prefix = showGroups ? "      " : "";
				prefix = prefix + (isAvailable(e.getKey()) ? "  " : " *");
				result
						.append(prefix)
						.append(String.join(", ", e.getValue()), AttributedStyle.BOLD)
						.append(": ")
						.append(e.getKey().getDescription())
						.append('\n');
			});
			if (showGroups) {
				result.append('\n');
			}
		});

		return result;
	}

	private Comparator<Entry<CommandRegistration, SortedSet<String>>> sortByFirstCommandName() {
		return Comparator.comparing(e -> e.getValue().first());
	}

	private boolean isAvailable(CommandRegistration methodTarget) {
		return true;
	}

	private void appendUnderlinedFormal(AttributedStringBuilder result, ParameterDescription description) {
		for (char c : description.formal().toCharArray()) {
			if (c != ' ') {
				result.append("" + c, AttributedStyle.DEFAULT.underline());
			}
			else {
				result.append(c);
			}
		}
	}

	private List<ParameterDescription> getParameterDescriptions(CommandRegistration registration) {
		List<CommandOption> options = registration.getOptions();
		List<ParameterDescription> descriptions = new ArrayList<>();

		for (CommandOption option : options) {

			ParameterDescription description = new ParameterDescription();
			if (option.getType() != null) {
				description.type(option.getType().toString());
				description.formal(option.getType().toClass().getSimpleName());
			}
			else {
				description.formal("");
			}
			description.help(option.getDescription());
			description.mandatoryKey(option.isRequired());
			if (option.getType() != null && option.getType().isAssignableFrom(boolean.class)) {
				description.defaultValue("false");
			}
			else {
				description.defaultValue(option.getDefaultValue());
			}

			List<String> keys = new ArrayList<>();
			if (option.getLongNames() != null) {
				for (String ln : option.getLongNames()) {
					keys.add("--" + ln);
				}
			}
			if (option.getShortNames() != null) {
				for (Character sn : option.getShortNames()) {
					keys.add("-" + String.valueOf(sn));
				}
			}
			description.keys(keys);

			descriptions.add(description);
		}

		// return Utils.createMethodParameters(registration.getTarget().getMethod())
		// 		.flatMap(mp -> getParameterResolver().filter(pr -> pr.supports(mp)).limit(1L)
		// 				.flatMap(pr -> pr.describe(mp)))
		// 		.collect(Collectors.toList());
		return descriptions;
	}

	private static class DummyContext implements MessageInterpolator.Context {

		private final ConstraintDescriptor<?> descriptor;

		private DummyContext(ConstraintDescriptor<?> descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		public ConstraintDescriptor<?> getConstraintDescriptor() {
			return descriptor;
		}

		@Override
		public Object getValidatedValue() {
			return null;
		}

		@Override
		public <T> T unwrap(Class<T> type) {
			return null;
		}
	}
}
