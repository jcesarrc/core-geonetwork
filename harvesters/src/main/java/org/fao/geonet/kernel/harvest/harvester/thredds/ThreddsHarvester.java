//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.thredds;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class ThreddsHarvester extends AbstractHarvester<HarvestResult>
{

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node, ServiceContext context) throws BadInputEx
	{
		params = new ThreddsParams(dataMan);
        super.setParams(params);

        params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Element node) throws BadInputEx, SQLException
	{
		params = new ThreddsParams(dataMan);
        super.setParams(params);

        //--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.setUuid(UUID.randomUUID().toString());

		String id = settingMan.add("harvesting", "node", getType());

		storeNode(params, "id:"+id);
        Source source = new Source(params.getUuid(), params.getName(), params.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.getUuid());
        
		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(String id, Element node)
									throws BadInputEx, SQLException
	{
		ThreddsParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(path);

		//--- update database
		storeNode(copy, path);

		//--- we update a copy first because if there is an exception Params
		//--- could be half updated and so it could be in an inconsistent state

        Source source = new Source(copy.getUuid(), copy.getName(), copy.getTranslations(), true);
        context.getBean(SourceRepository.class).save(source);
        Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.getUuid());
		
		params = copy;
        super.setParams(params);

    }

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		ThreddsParams params = (ThreddsParams) p;
        super.setParams(params);

        settingMan.add("id:"+siteId, "url",  params.url);
		settingMan.add("id:"+siteId, "icon", params.icon);
		settingMan.add("id:"+optionsId, "lang",  params.lang);
		settingMan.add("id:"+optionsId, "topic",  params.topic);
		settingMan.add("id:"+optionsId, "createThumbnails",  params.createThumbnails);
		settingMan.add("id:"+optionsId, "createServiceMd", params.createServiceMd);
		settingMan.add("id:"+optionsId, "createCollectionDatasetMd",  params.createCollectionDatasetMd);
		settingMan.add("id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd);
		settingMan.add("id:"+optionsId, "ignoreHarvestOnCollections",  params.ignoreHarvestOnCollections);
		settingMan.add("id:"+optionsId, "collectionGeneration",  params.collectionMetadataGeneration);
		settingMan.add("id:"+optionsId, "collectionFragmentStylesheet",  params.collectionFragmentStylesheet);
		settingMan.add("id:"+optionsId, "collectionMetadataTemplate",  params.collectionMetadataTemplate);
		settingMan.add("id:"+optionsId, "createCollectionSubtemplates",  params.createCollectionSubtemplates);
		settingMan.add("id:"+optionsId, "outputSchemaOnCollectionsDIF",  params.outputSchemaOnCollectionsDIF);
		settingMan.add("id:"+optionsId, "outputSchemaOnCollectionsFragments",  params.outputSchemaOnCollectionsFragments);
		settingMan.add("id:"+optionsId, "ignoreHarvestOnAtomics",  params.ignoreHarvestOnAtomics);
		settingMan.add("id:"+optionsId, "atomicGeneration",  params.atomicMetadataGeneration);
		settingMan.add("id:"+optionsId, "modifiedOnly",  params.modifiedOnly);
		settingMan.add("id:"+optionsId, "atomicFragmentStylesheet",  params.atomicFragmentStylesheet);
		settingMan.add("id:"+optionsId, "atomicMetadataTemplate",  params.atomicMetadataTemplate);
		settingMan.add("id:"+optionsId, "createAtomicSubtemplates",  params.createAtomicSubtemplates);
		settingMan.add("id:"+optionsId, "outputSchemaOnAtomicsDIF",  params.outputSchemaOnAtomicsDIF);
		settingMan.add("id:"+optionsId, "outputSchemaOnAtomicsFragments",  params.outputSchemaOnAtomicsFragments);
		settingMan.add("id:"+optionsId, "createAtomicDatasetMd",  params.createAtomicDatasetMd);
		settingMan.add("id:"+optionsId, "datasetCategory",  params.datasetCategory);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	public void doHarvest(Logger log) throws Exception
	{
		Harvester h = new Harvester(cancelMonitor, log, context, params);
		result = h.harvest(log);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private ThreddsParams params;
}
