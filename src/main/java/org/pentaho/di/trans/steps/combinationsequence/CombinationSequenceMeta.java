/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.combinationsequence;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 30-06-2008
 * 
 *  Sequence mode example
 * 
 *    Field1  Field2  RESET  INCREMENT
 *    A       B       1      1 
 *    A       B       2      1
 *    A       B       3      1
 *    A       C       1      2
 *    C       C       1      3
 *    C       C       2      3
 *    D       C       1      4
 */

@Step(id = "CombinationSequence", image = "combinationsequence.svg", i18nPackageName = "org.pentaho.di.trans.steps.combinationsequence", name = "CombinationSequence.Name", description = "CombinationSequence.Description", categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Experimental", // TODO:
																																																																													// Transform
																																																																													// step
																																																																													// category
		documentationUrl = "https://github.com/nadment/pdi-combinationsequence/wiki")

@InjectionSupported(localizationPrefix = "CombinationSequenceMeta.Injection.")

public class CombinationSequenceMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = CombinationSequenceMeta.class; // for i18n
																	// purposes,
																	// needed by
																	// Translator2!!

	// purposes,
	// needed by
	// Translator2!!

	@Injection(name = "MODE")
	private CombinationSequenceMode mode;

	@Injection(name = "FIELDS")
	private String[] fieldName;

	@Injection(name = "TARGET_FIELDNAME")
	private String resultfieldName;

	@Injection(name = "START")
	private String start;

	@Injection(name = "INCREMENT")
	private String increment;

	public CombinationSequenceMeta() {
		super(); // allocate BaseStepMeta
	}

	public CombinationSequenceMode getMode() {
		return mode;
	}

	public String getStart() {
		return start;
	}

	/**
	 * @return Returns the resultfieldName.
	 */
	public String getResultFieldName() {
		return resultfieldName;
	}

	/**
	 * @param resultName
	 *            The resultfieldName to set.
	 */
	public void setResultFieldName(String name) {
		this.resultfieldName = name;
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode);
	}

	@Override
	public Object clone() {
		CombinationSequenceMeta retval = (CombinationSequenceMeta) super.clone();

		int nrfields = fieldName.length;

		retval.allocate(nrfields);

		System.arraycopy(fieldName, 0, retval.fieldName, 0, nrfields);
		return retval;
	}

	public void allocate(int nrfields) {
		fieldName = new String[nrfields];
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String[] getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            The fieldName to set.
	 */
	public void setFieldName(String[] fieldName) {
		this.fieldName = fieldName;
	}

	public void setMode(CombinationSequenceMode mode) {
		this.mode = mode;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public void setIncrement(String increment) {
		this.increment = increment;
	}

	public String getIncrement() {
		return increment;
	}

	private void readData(Node stepnode) throws KettleXMLException {
		try {
			mode = CombinationSequenceMode.valueOf(XMLHandler.getTagValue(stepnode, "mode"));
			start = XMLHandler.getTagValue(stepnode, "start");
			increment = XMLHandler.getTagValue(stepnode, "increment");
			resultfieldName = XMLHandler.getTagValue(stepnode, "resultfieldName");

			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields = XMLHandler.countNodes(fields, "field");

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				fieldName[i] = XMLHandler.getTagValue(fnode, "name");
			}
		} catch (Exception e) {
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	@Override
	public String getXML() {
		StringBuilder xml = new StringBuilder();
		xml.append("      " + XMLHandler.addTagValue("mode", mode.name()));
		xml.append("      " + XMLHandler.addTagValue("start", start));
		xml.append("      " + XMLHandler.addTagValue("increment", increment));
		xml.append("      " + XMLHandler.addTagValue("resultfieldName", resultfieldName));

		xml.append("    <fields>" + Const.CR);
		for (int i = 0; i < fieldName.length; i++) {
			xml.append("      <field>" + Const.CR);
			xml.append("        " + XMLHandler.addTagValue("name", fieldName[i]));
			xml.append("      </field>" + Const.CR);
		}
		xml.append("      </fields>" + Const.CR);

		return xml.toString();
	}

	@Override
	public void setDefault() {
		mode = CombinationSequenceMode.RESET;
		resultfieldName = null;
		start = "1";
		increment = "1";
		int nrfields = 0;
		allocate(nrfields);
	}

	@Override
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
			throws KettleException {
		try {
			mode = CombinationSequenceMode.valueOf(rep.getStepAttributeString(id_step, "mode"));
			start = rep.getStepAttributeString(id_step, "start");
			increment = rep.getStepAttributeString(id_step, "increment");
			resultfieldName = rep.getStepAttributeString(id_step, "resultfieldName");
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");

			allocate(nrfields);

			for (int i = 0; i < nrfields; i++) {
				fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
			}
		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	@Override
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "mode", mode.name());
			rep.saveStepAttribute(id_transformation, id_step, "start", start);
			rep.saveStepAttribute(id_transformation, id_step, "increment", increment);
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldName", resultfieldName);
			for (int i = 0; i < fieldName.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
			}
		} catch (Exception e) {
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

	@Override
	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space, Repository repository, IMetaStore metaStore) {
		if (!Utils.isEmpty(resultfieldName)) {
			ValueMetaInterface v = new ValueMetaInteger(resultfieldName);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}

	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {
		CheckResult cr;
		String error_message = "";

		if (Utils.isEmpty(resultfieldName)) {
			error_message = BaseMessages.getString(PKG, "Meta.CheckResult.ResultFieldMissing");
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
		} else {
			error_message = BaseMessages.getString(PKG, "Meta.CheckResult.ResultFieldOK");
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
		}
		remarks.add(cr);

		if (prev == null || prev.size() == 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
					BaseMessages.getString(PKG, "Meta.CheckResult.NotReceivingFields"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "Meta.CheckResult.StepRecevingData", prev.size() + ""), stepMeta);
			remarks.add(cr);

			boolean error_found = false;
			error_message = "";

			// Starting from selected fields in ...
			for (int i = 0; i < fieldName.length; i++) {
				int idx = prev.indexOfValue(fieldName[i]);
				if (idx < 0) {
					error_message += "\t\t" + fieldName[i] + Const.CR;
					error_found = true;
				}
			}
			if (error_found) {
				error_message = BaseMessages.getString(PKG, "Meta.CheckResult.FieldsFound", error_message);

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			} else {
				if (fieldName.length > 0) {
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
							BaseMessages.getString(PKG, "Meta.CheckResult.AllFieldsFound"), stepMeta);
					remarks.add(cr);
				} else {
					cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
							BaseMessages.getString(PKG, "Meta.CheckResult.NoFieldsEntered"), stepMeta);
					remarks.add(cr);
				}
			}

		}

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "Meta.CheckResult.StepRecevingData2"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "Meta.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta);
			remarks.add(cr);
		}
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new CombinationSequenceStep(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new CombinationSequenceData();
	}

	@Override
	public boolean supportsErrorHandling() {
		return true;
	}

}