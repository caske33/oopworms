package worms.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import worms.util.ArrayUtil;
import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.max;
import be.kuleuven.cs.som.annotate.*;

/**
 * A class representing a game world with a width, height.
 * 
 * @invar	| isValidWidth(getWidth()) && isValidHeight(getHeight())
 * @invar	| isValidPassableMap(getPassableMap())
 * @invar	| hasProperFoods() && hasProperWorms() && hasProperProjectile();
 */
public class World {

	/**
	 * Creates a new world with a given width, height, passableMap and random number generator
	 * 
	 * @param width
	 * 				The width of the world (in meter)
	 * @param height
	 * 				The height of the world (in meter)
	 * @param passableMap
	 * 				A rectangular matrix indicating which parts of the terrain are passable and impassable. 
	 * 				This matrix is derived from the transparency of the pixels in the image file of the terrain.
	 * 				passableMap[r][c] is true if the location at row r and column c is passable, 
	 * 				and false if that location is impassable. 
	 * 				The elements in the first row (row 0) represent the pixels at the top of the terrain (i.e., largest y-coordinates). 
	 * 				The elements in the last row (row passableMap.length-1) represent pixels at the bottom of the terrain (smallest y-coordinates). 
	 * 				The elements in the first column (column 0) represent the pixels at the left of the terrain (i.e., smallest x-coordinates). 
	 * 				The elements in the last column (column passableMap[0].length-1) represent the pixels at the right of the terrain (i.e., largest x-coordinates).
	 * @param random
	 * 				A random number generator, seeded with the value obtained from the command line or from GUIOptions, 
	 * 				that can be used to randomize aspects of the world in a repeatable way.
	 * @post	| new.getWidth() == width
	 * @post	| new.getHeight() == height
	 * @effect	| setPassableMap(passableMap)
	 * @throws IllegalArgumentException
	 * 			| !isValidWidth(width) || !isValidHeight(height)
	 */
	@Raw
	public World(double width, double height, boolean[][] passableMap, Random random) throws IllegalArgumentException{
		// TODO Auto-generated constructor stub
		
		if(!isValidWidth(width) || !isValidHeight(height))
			throw new IllegalArgumentException();
		
		this.width = width;
		this.height = height;
		setPassableMap(passableMap);
	}
	
	/**
	 * Gets the width of this world (in metres).
	 */
	@Raw @Basic @Immutable
	public double getWidth(){
		return width;
	}
	
	/**
	 * Gets the height of this world (in metres).
	 */
	@Raw @Basic @Immutable
	public double getHeight(){
		return height;
	}
	
	/**
	 * Gets the upperbound to the width of worlds.
	 * 
	 * @return	| result >= 0
	 */
	public static double getWidthUpperBound(){
		return Double.MAX_VALUE;
	}
	
	/**
	 * Gets the upperbound to the height of worlds.
	 * 
	 * @return	| result >= 0
	 */
	public static double getHeightUpperBound(){
		return Double.MAX_VALUE;
	}
	
	/**
	 * Checks if the given width is a valid width.
	 * 
	 * @param width		The width to check
	 * @return			| result == (0 <= width && width <= getWidthUpperBound())
	 */
	public static boolean isValidWidth(double width){
		return 0 <= width && width <= getWidthUpperBound();
	}
	
	/**
	 * Checks if the given height is a valid height
	 * 
	 * @param height	The height to check
	 * @return			| result == (0 <= height && height <= getHeightUpperBound())
	 */
	public static boolean isValidHeight(double height){
		return 0 <= height && height <= getHeightUpperBound();
	}
	private double width,height;
	
	/**
	 * Checks if a position is inside the world boundaries.
	 * 
	 * @param pos	The position to check
	 * @return		| if(pos == null) result == false
	 * @return		| result == (0 <= pos.getX() && pos.getX() <= getWidth()
	 * 				|			&& 0 <= pos.getY() && pos.getY() <= getHeight())
	 */
	@Raw
	public boolean isInsideWorldBoundaries(Position pos){
		if(pos == null)
			return false;
		return 0 <= pos.getX() && pos.getX() <= getWidth()
				&& 0 <= pos.getY() && pos.getY() <= getHeight();
	}
	
	/**
	 * Checks if a circlular position (with radius) is inside the world boundaries.
	 * 
	 * @param pos		The position to check
	 * @param radius	The radius of the circle.
	 * @return			| if(pos == null) then result == false
	 * @return			| if(Double.isNaN(radius)) then result == false
	 * @return			| result == (isInsideWorldBoundaries(pos.offset(-radius, -radius)) && isInsideWorldBoundaries(pos.offset(radius,radius))
	 */
	@Raw
	public boolean isInsideWorldBoundaries(Position pos, double radius){
		if(pos == null)
			return false;
		if(Double.isNaN(radius))
			return false;
		return isInsideWorldBoundaries(pos.offset(-radius, -radius))
				&& isInsideWorldBoundaries(pos.offset(radius,radius));
	}

	/**
	 * Checks whether the circle with center pos and radius radius is only covering passable terrain.
	 * 
	 * @param pos		The center of the circle.
	 * @param radius	The radius of the circle.
	 * @return			| if(pos == null || Double.isNaN(radius)) result == false
	 * @return			| if(nbCellRows() == 0 || nbCellColumns() == 0) result == true
	 * @return			If there's an impassable position in the world that lies within the circle, than the result is false.
	 * 					| if( 
	 * 					|		for some Position other in Position:
	 * 					|			other.squaredDistance(pos) < Math.pow(radius,2)
	 * 					|			&& world.isInsideWorldBoundaries(pos)
	 * 					|			&& !getPassableMap()[(int)floor(getCellRowCoordinate(other.getY()))][(int)floor(getCellColumnCoordinate(other.getX()))]
	 * 					|  )
	 * 					|	then result == false
	 * 					| 	else result == true
	 */
	public boolean isPassablePosition(Position pos, double radius){
		/*
		 * Strategy for this method:
		 * 1) Find the horizontal grid lines (of the passableMap) that intersect with the circle
		 * 2) For each line:
		 * 3)		Find the 2 intersections between the horizontal line and the circle
		 * 4)		Find the grid cells that lie within these boundaries between this line and the next
		 * 5)		Check if any of them are impassable.
		 */
		if(pos == null)
			return false;
		if(Double.isNaN(radius))
			return false;
		
		if(nbCellRows() == 0 || nbCellColumns() == 0)
			 return true;
		
		boolean[][] map = getPassableMap();
		
		//Get the rows in which the circle lies
		int minRow = (int)floor(getCellRowCoordinate(pos.getY()+radius));
		int maxRow = (int)ceil(getCellRowCoordinate(pos.getY()-radius));
		//Get the row in which the center of the circle lies.
		int horizontalRow = (int)floor(getCellRowCoordinate(pos.getY()));
		
		double x0 = pos.getX();
		double y0 = pos.getY();
		
		double nextLocalRadius = 0, localRadius = 0;
		for (int i = minRow; i < maxRow; i++) {
			if(i < 0 || i >= map.length)
				continue;
			//Get the 'radius' (half of the chord) at the level of the next row(line). 
			//('radius' = farthest offset from x0 at the level of that row(line).
			if(i != maxRow -1)
				nextLocalRadius = Math.sqrt(Math.pow(radius,2)-Math.pow(getYCoordinate(i+1)-y0,2));
			
			//Get the first and last column that overlaps with the circle in this row.
			int minColumn = (int)floor(min(getCellColumnCoordinate(x0 - nextLocalRadius), getCellColumnCoordinate(x0 - localRadius)));
			int maxColumn = (int)ceil(max(getCellColumnCoordinate(x0 + nextLocalRadius), getCellColumnCoordinate(x0 + localRadius)));
			
			
			//Circle crosses a Vertical Line twice within one grid row.
			//Only possible at the row of the center of the circle.
			if(i == horizontalRow){
				while(x0-getXCoordinate(minColumn) < radius)
					minColumn--;
				while(getXCoordinate(maxColumn)-x0 < radius)
					maxColumn++;
			}				
			
			for(int j = minColumn; j < maxColumn; j++){
				if(j < 0 || j >= map[i].length)
					continue;
				if(map[i][j] == false)
					return false;
			}
			localRadius = nextLocalRadius;
		}
		return true;
	}
	
	/**
	 * Returns the number of cells in the width of the passableMap of this world.
	 * 
	 * @return	| if(nbCellRows() == 0) 
	 * 			| 		then result == 0
	 * 			| else 
	 * 			|		result == getPassableMap()[0].length
	 */
	public int nbCellColumns(){
		boolean[][] map = getPassableMap();
		if(map.length == 0)
			return 0;
		return map[0].length;
	}
	
	/**
	 * Returns the number of cells in the height of the passableMap of this world.
	 * 
	 * @return	| result == getPassableMap().length
	 */
	public int nbCellRows(){
		return getPassableMap().length;
	}
	
	/**
	 * Returns the width of a cell of the passableMap of this world (in metres).
	 * 
	 * @return	| result == getWidth()/nbCellColumns()
	 */
	public double cellWidth(){
		return getWidth()/nbCellColumns();
	}
	
	/**
	 * Returns the height of a cell of the passableMap of this world (in metres).
	 * 
	 * @return	| result == getHeight()/nbCellRows()
	 */
	public double cellHeight(){
		return getHeight()/nbCellRows();
	}
	
	/**
	 * Transforms x-coordinates (metres) to the scale of cellcoordinates for the passableMap of this world.
	 * 
	 * @param x		The x-coordinate to transform (in metres).
	 * @return		| result == (x / cellWidth())
	 * @note		The result of this method is a double and can be a non-integer number.
	 */
	public double getCellColumnCoordinate(double x){
		return (x / cellWidth());
	}
	
	/**
	 * Transforms y-coordinates (metres) to the scale of cellcoordinates for the passableMap of this world
	 * 
	 * @param y		The y-coordinate to transform (in metres).
	 * @return		| result == (nbCellRows() - (y / cellHeight()))
	 * @note		The result of this method is a double and can be a non-integer number.
	 */
	public double getCellRowCoordinate(double y){
		return nbCellRows() - (y / cellHeight());
	}
	
	/**
	 * Transforms cell(column)coordinates (for the passableMap of this world) to the scale of x-coordinates (in metres).
	 * 
	 * @param columnCoordinate		The cell(column)coordinate
	 * @return						| result == cellWidth()*columnCoordinate
	 */
	public double getXCoordinate(int columnCoordinate){
		return cellWidth()*columnCoordinate;
	}
	
	/**
	 * Transforms cell(row)coordinates (for the passableMap of this world) to the scale of y-coordinates (in metres).
	 * 
	 * @param rowCoordinate		The cell(row)coordinate
	 * @return						| result == getHeight()-cellHeight()*rowCoordinate
	 */
	public double getYCoordinate(int rowCoordinate){
		return getHeight()-cellHeight()*rowCoordinate;
	}
	
	/**
	 * Gets the locationtype of a given position and for an entity with a given radius.
	 * 
	 * @param pos		The position to check.
	 * @param radius	The radius of the entity.
	 * @return			| if(!isPassablePosition(pos,radius)) then result == IMPASSABLE
	 * 					| else if(!isPassablePosition(pos,radius*1.1)) then result == CONTACT
	 * 					| else result == PASSABLE
	 */
	public LocationType getLocationType(Position pos, double radius){
		if(!isPassablePosition(pos,radius))
			return LocationType.IMPASSABLE;
		if(!isPassablePosition(pos,radius*1.1))
			return LocationType.CONTACT;
		return LocationType.PASSABLE;
	}
	
	/**
	 * Returns (a deep clone of) the passable map for this world 
	 * or null if the passableMap isn't set yet (raw state).
	 */
	@Raw @Basic
	protected boolean[][] getPassableMap(){
		if(passableMap == null)
			return null;
		return ArrayUtil.deepClone(passableMap);
	}
	
	/**
	 * Checks if the given passable map is a valid passable map.
	 * 
	 * @param passableMap	The map to check
	 * @return		| if(passableMap == null)
	 * 				|	then result == false
	 * @return		| result == (for each boolean[] row in passableMap: (row.length == passableMap[0].length))
	 */
	public static boolean isValidPassableMap(boolean[][] passableMap){
		if(passableMap == null)
			return false;
		for (int i = 1; i < passableMap.length; i++) {
			if(passableMap[i].length != passableMap[0].length)
				return false;
		}
		return true;
	}
	
	/**
	 * Sets the passable map for this world.
	 * 
	 * @param passableMap The map to set
	 * @post	| if(!isValidPassableMap(passableMap))
	 * 			|		then ArrayUtil.deepEquals(new.getPassableMap(),new boolean[][]{})
	 * 			| 		else ArrayUtil.deepEquals(new.getPassableMap(),passableMap)
	 */
	@Raw @Model
	private void setPassableMap(boolean[][] passableMap){
		if(!isValidPassableMap(passableMap))
			this.passableMap = new boolean[][]{};
		else
			this.passableMap = ArrayUtil.deepClone(passableMap);
	}
	
	private boolean[][] passableMap;

	/**
	 * Checks whether this world is terminated.
	 */
	@Basic @Raw
	public boolean isTerminated(){
		return isTerminated;
	}
	
	/**
	 * Terminate this world
	 *
	 * @post	This world is terminated
	 * 			| new.isTerminated()
	 * @post	All entities in this world are terminated and removed.
	 *			| (for each worm in getWorms():
	 *			|	worm.isTerminated() && !new.hasAsEntity(worm))
	 *			| && (for each food in getFoods():
	 *			|	food.isTerminated() && !new.hasAsEntity(food))
	 *			| && (if hasProjectile()) 
	 *			|	then getProjectile().isTerminated() && !new.hasAsEntity(getProjectile())
	 */
	public void terminate(){
		if(!isTerminated()){
			Set<Entity> entities = getEntities();
			
			worms.clear();
			foods.clear();
			setProjectile(null);
			
			for (Entity entity : entities)
				entity.terminate();
			isTerminated = true;
		}
	}

	private boolean isTerminated = false;
	
	/**
	 * Returns a set of all food rations, all worms and the projectile in this world.
	 *  
	 * @return	An (unordered) set off all entities in this world (and only those).
	 *			| for each entity in result: entity == getProjectile() || getWorms().contains(entity) || getFoods().contains(entity)
	 *			| && for each worm in getWorms(): result.contains(worm)
	 *			| && for each food in getFoods(): result.contains(food)
	 *			| && if(hasProjectile()): result.contains(getProjectile())
	 */
	public Set<Entity> getEntities(){
		Set<Entity> result = new HashSet<Entity>();
		result.addAll(getFoods());
		result.addAll(getWorms());
		if(hasProjectile())
			result.add(getProjectile());
		return result;
	}
	
	/**
	 * Checks whether this world contains the given entity.
	 * 
	 * @param entity	The entity to check.
	 * @return	Whether the given entity registered in this world.
	 * 			| if(entity instanceof Food)
	 *			| 	return hasAsFood((Food) entity);
	 *			| if(entity instanceof Worm)
	 *			| 	return hasAsWorm((Worm) entity);
	 *			| if(entity instanceof Projectile)
	 *			| 	return entity == getProjectile();
	 *			| else
	 *			|	return false;
	 */
	public boolean hasAsEntity(Entity entity){
		if(entity instanceof Food)
			return hasAsFood((Food) entity);
		if(entity instanceof Worm)
			return hasAsWorm((Worm) entity);
		if(entity instanceof Projectile)
			return entity == getProjectile();
		return false;
	}
	
	/**
	 * Removes the entity from this world.
	 * 
	 * @param entity			The entity to remove.
	 * @effect					The entity is removed from the proper collection.
	 * 							| if(entity instanceof Food) then removeFood((Food) entity);
	 * 							| if(entity instanceof Worm) then removeWorm((Worm) entity);
	 * 							| if(entity instanceof Projectile) then removeProjectile();
	 * @throws ClassCastException
	 * 							| !(entity instanceof Food) && !(entity instanceof Worm) && !(entity instanceof Projectile)
	 * @throws IllegalArgumentException
	 * 							| entity instanceof Projectile && entity != getProjectile()
	 */
	public void removeAsEntity(Entity entity) throws IllegalArgumentException,ClassCastException{
		//FIXME tests
		if(entity instanceof Food)
			removeFood((Food) entity);
		else if(entity instanceof Worm)
			removeWorm((Worm) entity);
		else if(entity instanceof Projectile){
			if(entity == getProjectile())
				removeProjectile();
			else
				throw new IllegalArgumentException();
		}else
			throw new ClassCastException();
	}

	/**
	 * Returns the food rations in this world.
	 * 
	 */
	@Basic
	public Set<Food> getFoods(){
		return new HashSet<Food>(foods);
	}

	/**
	 * Checks whether this world can have the given food ration
	 * as one of its food rations.
	 * 
	 * @param	food
	 *			The food to check.
	 * @return	True iff the given food is effective,
	 *			the given food is not terminated and this world is not terminated.
	 *			| result ==
	 *			|   ((food != null) && (!food.isTerminated()) && (!this.isTerminated()))
	 */
	@Raw
	public boolean canHaveAsFood(Food food){
		return ((food != null) && (!food.isTerminated()) && (!this.isTerminated()));
	}

	/**
	 * Checks whether this world has proper food rations attached to it.
	 * 
	 * @return True iff this world can have each of the
	 *         foods attached to it as a food at its index,
	 *         and if each of these foods references this world as
	 *         the world to which they are attached.
	 *       | result ==
	 *       |   (for each food in getFoods():
	 *       |     (this.canHaveAsFood(food) && (food.getWorld() == this)))
	 * @note	Distinctiveness of this world's foods is gueranteed by java.util.Set<>.
	 */
	@Raw
	public boolean hasProperFoods() {
		for (Food food : foods)
			if(!canHaveAsFood(food) || food.getWorld() != this)
				return false;
		return true;
	}

	/**
	 * Checks whether this world has the given food as one of its
	 * foods.
	 * 
	 * @param  food
	 * 		   The food to check.
	 * @return The given food is registered at some position as
	 *         a food of this world.
	 *       | result == (getFoods().contains(food))
	 */
	public boolean hasAsFood(@Raw Food food){
		return foods.contains(food);
		// Just as in the Share-Purchase example,
		// a more efficient implementation would be possible if
		// the consistency imposed on the bi-directional association
		// would be guaranteed.
		// return (food != null) && (food.getWorld() == this);
	}

	/**
	 * Adds the given food to the list of foods of this world.
	 * 
	 * @param	food
	 * 			The food to be added.
	 * @post	The number of foods in this world is
	 * 			incremented by 1.
	 * 			| new.getNbFoods() == getNbFoods()+1
	 * @post	The given food references this world.
	 *			| (new food).getWorld() == this
	 * @post	The food is added to the list of foods for this world.
	 * 			| new.hasAsFood(food)
	 * @throws	IllegalArgumentException
	 *			| (!canHaveAsFood(food) || (this.hasAsFood(food)))
	 * @throws 	IllegalStateException
	 * 			| food.hasWorld()
	 */
	void addFood(@Raw Food food) throws IllegalArgumentException,IllegalStateException{
		if(!canHaveAsFood(food) || (this.hasAsFood(food)))
			throw new IllegalArgumentException();
		if(food.hasWorld())
			throw new IllegalStateException();
		foods.add(food);
		food.setWorld(this);
	}

	/**
	 * Removes the given food from the list of foods of this world.
	 * 
	 * @param	food
	 * 			The food to be removed.
	 * @post	The number of foods of this world is
	 * 			decremented by 1.
	 * 			| new.getNbFoods() == getNbFoods() - 1
	 * @post	This world no longer has the given food as
	 * 			one of its foods.
	 * 			| !new.hasAsFood(food)
	 * @effect	The given food is terminated
	 *			(and thus no longer references this world).
	 *			| food.terminate()
	 * @throws	IllegalArgumentException
	 *			The given food is not effective or this world
	 *			does not have the given food as one of its foods.
	 *			| ((food == null) || (!this.hasAsFood(food)))
	 */
	@Raw
	void removeFood(Food food) throws IllegalArgumentException{
		if((food == null) || (!this.hasAsFood(food)))
			throw new IllegalArgumentException();
		foods.remove(food);
		food.terminate();
	}

	/**
	 * An (unordered) set containing all the food rations in this world.
	 * 
	 * @invar	The referenced set is effective.
	 * 			| foods != null
	 * @invar	Each food registered in the referenced set
	 * 			is effective and not terminated.
	 * 			| for each food in foods:
	 * 			|	(food != null && !food.isTerminated())
	 * @invar	No food is registered more than once
	 *       	in the referenced collection.
	 *       	(This is gueranteed by java.util.Set<>.)
	 *       	| let food_list == new ArrayList(foods) in:
	 *			|	(for each i,j in 0..food_list.size()-1:
	 *			|		((i == j) ||
	 *			|		(food_list.get(i) != food_list.get(j)))
	 */
	private final Set<Food> foods = new HashSet<Food>();

	/**
	 * Returns all the worms in this world.
	 */
	@Basic
	public List<Worm> getWorms(){
		return new ArrayList<Worm>(worms);
	}

	/**
	 * Returns the worm associated with this world at the
	 * given index.
	 * 
	 * @param	index
	 *			The (0-based) index of the worm to return.
	 * @throws	IndexOutOfBoundsException
	 * 			The given index is negative or it exceeds or equals the
	 * 			number of worms in this world.
	 *			| (index < 0) || (index >= getNbWorms())
	 */
	@Basic
	@Raw
	public Worm getWormAt(int index) throws IndexOutOfBoundsException{
		return worms.get(index);
	}

	/**
	 * Returns the number of worms in this world.
	 */
	@Basic
	@Raw
	public int getNbWorms(){
		return worms.size();
	}

	/**
	 * Checks whether this world can have the given worm
	 * as one of its worms.
	 * 
	 * @param	worm
	 *			The worm to check.
	 * @return	True iff the given worm is effective,
	 *			the given worm is not terminated and this world is not terminated.
	 *			| result ==
	 *			|   ((worm != null) && (!worm.isTerminated()) && (!this.isTerminated()))
	 */
	@Raw
	public boolean canHaveAsWorm(Worm worm){
		return ((worm != null) && (!worm.isTerminated()) && (!this.isTerminated()));
	}

	/**
	 * Checks whether this world can have the given worm
	 * as one of its worms at the given (1-based) index.
	 * 
	 * @param  worm
	 *         The worm to check.
	 * @return False if the given index is negative or exceeds the
	 *         number of worms for this world.
	 *       | if ((index < 0) || (index > getNbWorms()))
	 *       |   then result == false
	 *         Otherwise, false if this world cannot have the given
	 *         worm as one of its worms.
	 *       | else if ( ! this.canHaveAsWorm(worm) )
	 *       |   then result == false
	 *         Otherwise, true iff the given worm is
	 *         not registered at another index than the given index.
	 *       | else result ==
	 *       |   (for each i in 0..getNbWorms()-1:
	 *       |     (i == index) || (getWormAt(i) != worm))
	 */
	@Raw
	public boolean canHaveAsWormAt(Worm worm, int index){
		if ((index < 0) || (index > getNbWorms()))
			return false;
		if (!this.canHaveAsWorm(worm))
			return false;
		for (int i = 0; i < getNbWorms(); i++)
			if ((i != index) && (getWormAt(i) == worm))
				return false;
		return true;
	}

	/**
	 * Checks whether this world has proper worms attached to it.
	 * 
	 * @return True iff this world can have each of the
	 *         worms attached to it as a worm at its index,
	 *         and if each of these worms references this world as
	 *         the world to which they are attached.
	 *       | result ==
	 *       |   (for each i in 0..getNbWorms()-1:
	 *       |     (this.canHaveAsWormAt(worm,i) &&
	 *       |       (worm.getWorld() == this)))
	 */
	public boolean hasProperWorms() {
		for (int i = 0; i < getNbWorms(); i++){
			if (!canHaveAsWormAt(getWormAt(i), i))
				return false;
			if (getWormAt(i).getWorld() != this)
				return false;
		}
		return true;
	}

	/**
	 * Checks whether this world has the given worm as one of its
	 * worms.
	 * 
	 * @param  worm
	 * 		   The worm to check.
	 * @return The given worm is registered at some position as
	 *         a worm of this world.
	 *       | for some i in 0..getNbWorms()-1:
	 *       |   getWormAt(i) == worm
	 */
	public boolean hasAsWorm(@Raw Worm worm){
		return worms.contains(worm);
		// Just as in the Share-Purchase example,
		// a more efficient implementation would be possible if
		// the consistency imposed on the bi-directional association
		// would be guaranteed.
		// return (worm != null) && (worm.getWorld() == this);
	}

	/**
	 * Adds the given worm to the list of worms of this world.
	 * 
	 * @param	worm
	 * 			The worm to be added.
	 * @post	The number of worms in this world is
	 * 			incremented by 1.
	 * 			| new.getNbWorms() == getNbWorms()+1
	 * @post	This world has the given worm as its last worm.
	 * 			| new.getWormAt(getNbWorms()) == worm
	 * @post	The given worm references this world.
	 *			| (new worm).getWorld() == this
	 * @throws	IllegalArgumentException
	 *			| (!canHaveAsWormAt(worm, getNbWorms()) || (this.hasAsWorm(worm)))
	 * @throws	IllegalStateException
	 * 			| worm.hasWorld()
	 */
	void addWorm(@Raw Worm worm) throws IllegalArgumentException, IllegalStateException{
		if(!canHaveAsWormAt(worm, getNbWorms()) || (this.hasAsWorm(worm)))
			throw new IllegalArgumentException();
		if(worm.hasWorld())
			throw new IllegalStateException();
		worms.add(worm);
		worm.setWorld(this);
	}

	/**
	 * Removes the given worm from the list of worms of this world.
	 * 
	 * @param	worm
	 * 			The worm to be removed.
	 * @post	The number of worms of this world is
	 * 			decremented by 1.
	 * 			| new.getNbWorms() == getNbWorms() - 1
	 * @post	This world no longer has the given worm as
	 * 			one of its worms.
	 * 			| ! new.hasAsWorm(worm)
	 * @effect	The given worm is terminated
	 *			(and thus no longer references this world).
	 *			| worm.terminate()
	 * @post	All worms registered at an index beyond the index at
	 * 			which the given worm was registered, are shifted
	 * 			one position to the left.
	 * 			| (for each i,j in 0..getNbWorms()-1:
	 * 			|   if ((getWormAt(i) == worm) && (i < j))
	 * 			|     then new.getWormAt(j-1) == getWormAt(j))
	 * @throws	IllegalArgumentException
	 *			The given worm is not effective or this world
	 *			does not have the given worm as one of its worms.
	 *			| ((worm == null) || (this.hasAsWorm(worm)))
	 */
	@Raw
	void removeWorm(Worm worm) throws IllegalArgumentException{
		if((worm == null) || (!this.hasAsWorm(worm)))
			throw new IllegalArgumentException();
		worms.remove(worm);
		worm.terminate();
	}

	/**
	 * An ordered list containing all the worms in this world.
	 * 
	 * @invar	The referenced list is effective.
	 * 			| worms != null
	 * @invar	Each worm registered in the referenced list
	 * 			is effective and not terminated.
	 * 			| for each worm in worms:
	 * 			|	(worm != null && !worm.isTerminated())
	 * @invar	No worm is registered at several positions
	 *       	in the referenced list.
	 *			| for each i,j in 0..worms.size()-1:
	 *			|    ((i == j) ||
	 *			|     (worms.get(i) != worms.get(j))
	 */
	private final List<Worm> worms = new ArrayList<Worm>();

	/** 
	 * Return the live projectile of this world.
	 * A null reference is returned if this world currently
	 * has no live projectile.
	 */
	@Basic @Raw
	public Projectile getProjectile() {
		return this.projectile;
	}

	/**
	 * Check whether this world can have the given projectile as
	 * its projectile.
	 *
	 * @param  projectile
	 *         The projectile to check.
	 * @return True if the projectile is null or this world and the given projectile are not terminated.
	 *       | result == ((projectile == null) || (!projectile.isTerminated() && !this.isTerminated()))
	 */
	@Raw
	public boolean canHaveAsProjectile(@Raw Projectile projectile) {
		return (projectile == null) || (!projectile.isTerminated() && !this.isTerminated());
	}

	/**
	 * Check whether this world has a proper projectile.
	 *
	 * @return True iff this world can have its current projectile
	 *         as projectile, and if that projectile, if it is effective, 
	 * 		   references this world.
	 *       | result ==
	 *       |   (canHaveAsProjectile(getProjectile() &&
	 *       |	  (!hasProjectile() ||
	 *       |		  (getProjectile().getWorld() == this)))
	 */
	@Raw
	public boolean hasProperProjectile() {
		return (canHaveAsProjectile(getProjectile()) &&
				(!hasProjectile() ||
					(getProjectile().getWorld() == this)));
	}

	/** 
	 * Register the given projectile as the projectile of this world.
	 * 
	 * @param  projectile
	 *         The projectile to be registered.
	 * @post 	| new.getProjectile() == projectile
	 * @post 	| if(projectile != null) then (new projectile).getWorld() == this
	 * @effect	| if(hasProjectile()) getProjectile().terminate()
	 * @throws IllegalArgumentException
	 *			| !canHaveAsProjectile(projectile)
	 * @throws IllegalStateException
	 * 			| (projectile != null) && projectile.hasWorld()
	 */
	@Raw
	public void setProjectile(@Raw Projectile projectile) throws IllegalArgumentException,IllegalStateException {
		if(!canHaveAsProjectile(projectile))
			throw new IllegalArgumentException();
		if(projectile != null && projectile.hasWorld())
			throw new IllegalStateException();
		
		Projectile oldProjectile = this.projectile;
		this.projectile = projectile;
		if(projectile != null)
			projectile.setWorld(this);
		if(oldProjectile != null)
			oldProjectile.terminate();
	}
	
	/**
	 * Disassociates this world's projectile.
	 *
	 * @effect	| setProjectile(null)
	 */
	public void removeProjectile() {
		setProjectile(null);
	}
	/**
	 * Checks if this world has a projectile
	 * 
	 * @return	| result == (getProjectile() != null)
	 */
	public boolean hasProjectile(){
		return getProjectile() != null;
	}

	/**
	 * The single live Projectile of this world, or null if there is none.
	 */
	private Projectile projectile = null;
}
