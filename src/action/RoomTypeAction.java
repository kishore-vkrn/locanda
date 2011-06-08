package action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Extra;
import model.Image;
import model.RoomFacility;
import model.RoomType;
import model.Structure;
import model.User;
import model.internal.Message;
import model.listini.Convention;
import model.listini.ExtraPriceList;
import model.listini.ExtraPriceListItem;
import model.listini.RoomPriceList;
import model.listini.RoomPriceListItem;
import model.listini.Season;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;

import service.RoomTypeService;
import service.StructureService;
import service.StructureServiceImpl;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage(value="default")
public class RoomTypeAction extends ActionSupport implements SessionAware{
	private Map<String, Object> session = null;
	private Message message = new Message();
	private List<RoomType> roomTypes;
	private RoomType roomType = null;
	private Image image = null;
	private List<RoomFacility> roomTypeFacilities = null;
	private List<Integer> roomTypeFacilitiesIds = new ArrayList<Integer>();
	@Autowired
	private StructureService structureService = null;
	@Autowired
	private RoomTypeService roomTypeService;
	
	@Actions({
		@Action(value="/findAllRoomTypes",results = {
				@Result(name="success",location="/roomTypes.jsp")
		})
	})
	public String findAllRoomTypes() {
		User user = null;
		Structure structure = null;
		
		user = (User)this.getSession().get("user");
		structure = user.getStructure();
		
		this.setRoomTypes(structure.getRoomTypes());
		this.setRoomTypeFacilities(structure.getRoomFacilities());
		return SUCCESS;
	}
	
	@Actions({
		@Action(value="/goUpdateRoomType",results = {
				@Result(name="success",location="/roomType_edit.jsp")
		})		
	})
	public String goUpdateRoomType() {
		User user = null;
		Structure structure = null;
		
		user = (User)this.getSession().get("user");
		structure = user.getStructure();
		//this.setRoomType(structure.findRoomTypeById(this.getRoomType().getId()));
		this.setRoomType(this.getRoomTypeService().findRoomTypeById(structure,this.getRoomType().getId()));
		this.setRoomTypeFacilities(structure.getRoomFacilities());
		for(RoomFacility each: this.getRoomType().getRoomTypeFacilities()){			
			this.getRoomTypeFacilitiesIds().add(each.getId());		//popolo l'array roomFacilitiesIds con gli id delle Facilities già presenti nella Room da editare
		}
		return SUCCESS;
	}
	
	@Actions({
		@Action(value="/saveUpdateRoomType",results = {
				@Result(type ="json",name="success", params={
						"root","message"
				})
		})
	})
	public String saveUpdateRoomType(){
		User user = null;
		Structure structure = null;
		RoomType oldRoomtype = null;
		List<RoomFacility> checkedFacilities = null;
		
		
		user = (User)session.get("user");
		structure = user.getStructure();
		
		
		//checkedFacilities = structure.findRoomFacilitiesByIds(this.getRoomTypeFacilitiesIds());
		checkedFacilities = this.getStructureService().findRoomFacilitiesByIds(structure,this.getRoomTypeFacilitiesIds());
		//oldRoomtype = structure.findRoomTypeById(this.getRoomType().getId());
		oldRoomtype = this.getRoomTypeService().findRoomTypeById(structure,this.getRoomType().getId());
		if(oldRoomtype == null){
			//Si tratta di una aggiunta
			this.getRoomType().setId(structure.nextKey());
			this.getRoomType().setRoomTypeFacilities(checkedFacilities);
			//structure.addRoomType(this.getRoomType());
			this.getRoomTypeService().insertRoomType(structure, this.getRoomType());
			this.getStructureService().refreshPriceLists(structure);
			this.getMessage().setResult(Message.SUCCESS);
			this.getMessage().setDescription("Room Type added successfully");
			
		}else{
			//Si tratta di un update
			this.getRoomType().setRoomTypeFacilities(checkedFacilities);
			//structure.updateRoomType(this.getRoomType());
			this.getRoomTypeService().updateRoomType(structure, this.getRoomType());
			this.getMessage().setResult(Message.SUCCESS);
			this.getMessage().setDescription("Room Type updated successfully");
		}
		return SUCCESS;		
	}
	
	@Actions({
		@Action(value="/deleteRoomType",results = {
				@Result(type ="json",name="success", params={
						"root","message"
				}),
				@Result(type ="json",name="error", params={
						"root","message"
				})
		})
	})
	public String deleteRoomType(){
		User user = null;
		Structure structure = null;
		RoomType currentRoomType = null;
		
		user = (User)this.getSession().get("user");
		structure = user.getStructure();
		//currentRoomType = structure.findRoomTypeById(this.getRoomType().getId());
		currentRoomType = this.getRoomTypeService().findRoomTypeById(structure,this.getRoomType().getId());
		//if(structure.removeRoomType(currentRoomType)){
		if(this.getRoomTypeService().removeRoomType(structure, currentRoomType) >0){
			this.getMessage().setResult(Message.SUCCESS);
			this.getMessage().setDescription("Room Type removed successfully");
			return SUCCESS;
		}else{
			this.getMessage().setResult(Message.ERROR);
			this.getMessage().setDescription("Error deleting Room Type");
			return ERROR;
		}
	}
	
	
	@Actions({
		@Action(value="/deletePhotoRoomType",results = {
				@Result(type ="json",name="success", params={
						"root","message"
				} ),
				@Result(type ="json",name="error", params={
						"root","message"
				} )
		})
		
	})
	public String deletePhotoRoomType() {
		User user = null; 
		Structure structure = null;
		RoomType aRoomType = null;
		user = (User)this.getSession().get("user");
		//Controllare che sia diverso da null in un interceptor
		structure = user.getStructure();
		
		//aRoomType = structure.findRoomTypeById(this.getRoomType().getId());
		aRoomType = this.getRoomTypeService().findRoomTypeById(structure,this.getRoomType().getId());
		
		if(aRoomType.deleteImage(this.getImage())){
			this.getMessage().setResult(Message.SUCCESS);
			this.getMessage().setDescription("La foto e' stata cancellata con successo");
			return "success";
		}else{
			this.getMessage().setResult(Message.ERROR);
			this.getMessage().setDescription("Non e' stato possibile cancellare la foto");
			return "error";
		}		
	}
	
	public Map<String, Object> getSession() {
		return session;
	}
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;		
	}	
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public List<RoomType> getRoomTypes() {
		return roomTypes;
	}
	public void setRoomTypes(List<RoomType> roomTypes) {
		this.roomTypes = roomTypes;
	}
	public RoomType getRoomType() {
		return roomType;
	}
	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public List<Integer> getRoomTypeFacilitiesIds() {
		return roomTypeFacilitiesIds;
	}
	public void setRoomTypeFacilitiesIds(List<Integer> roomFacilitiesIds) {
		this.roomTypeFacilitiesIds = roomFacilitiesIds;
	}	
	public List<RoomFacility> getRoomTypeFacilities() {
		return roomTypeFacilities;
	}
	public void setRoomTypeFacilities(List<RoomFacility> roomTypeFacilities) {
		this.roomTypeFacilities = roomTypeFacilities;
	}

	public StructureService getStructureService() {
		return structureService;
	}

	public void setStructureService(StructureService structureService) {
		this.structureService = structureService;
	}

	public RoomTypeService getRoomTypeService() {
		return roomTypeService;
	}

	public void setRoomTypeService(RoomTypeService roomTypeService) {
		this.roomTypeService = roomTypeService;
	}
	
	
}