/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyacore.wrapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Date;

/**
 * Abstract base class for persons. We can't use {@link org.openmrs.module.kenyacore.wrapper.AbstractCustomizableWrapper}
 * for persons as they are not Customizable despite having attributes. See TRUNK-4231.
 */
public abstract class AbstractPersonWrapper extends AbstractObjectWrapper<Person> {

	/**
	 * Creates a new person wrapper
	 * @param target the target
	 */
	public AbstractPersonWrapper(Person target) {
		super(target);
	}

	/**
	 * Gets the value of the first attribute of the given type
	 * @param attrTypeUuid the attribute type UUID
	 * @return the value or null
	 */
	protected String getAsAttribute(String attrTypeUuid) {
		PersonAttribute attr = findFirstAttribute(attrTypeUuid);
		return attr != null ? attr.getValue() : null;
	}

	/**
	 * Sets a attribute value of this person. If the value is blank then any existing attribute will be voided.
	 * @param attrTypeUuid the attribute type UUID
	 * @param value the value
	 */
	protected void setAsAttribute(String attrTypeUuid, String value) {
		PersonAttribute attr = findFirstAttribute(attrTypeUuid);

		if (StringUtils.isNotBlank(value)) {
			if (attr == null) {
				attr = new PersonAttribute();
				attr.setAttributeType(MetadataUtils.getPersonAttributeType(attrTypeUuid));
				attr.setPerson(target);
				target.addAttribute(attr);
			}

			attr.setValue(value);
		}
		else if (attr != null) {
			attr.setVoided(true);
			attr.setDateVoided(new Date());
			attr.setVoidedBy(Context.getAuthenticatedUser());
		}
	}

	/**
	 * Gets the value of the first active attribute of the given type. By using the UUID of the attribute type this
	 * avoids forcing us to keep re-fetching attribute types via service methods which can be slow.
	 * @param attrTypeUuid the attribute type UUID
	 * @return the value or null
	 */
	protected PersonAttribute findFirstAttribute(String attrTypeUuid) {
		if (target.getAttributes() != null) {
			for (PersonAttribute attr : target.getAttributes())
				if (attr.getAttributeType().getUuid().equals(attrTypeUuid) && !attr.isVoided()) {
					return attr;
				}
		}
		return null;
	}
}