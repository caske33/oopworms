package worms.model;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Raw;

/**
 * A class of objects taking up space at a certain position in a game world.
 * 
 * @invar 	The position of the worm is a valid position. 
 * 		 	| Position.isValidPosition(getPosition())
 * @invar	The radius of the worm is a valid radius for this worm.
 * 			| canHaveAsRadius(getRadius())
 * @invar	This entity has a proper world.
 * 			| hasProperWorld()
 */
public abstract class Entity {
	/**
	 * Returns the x-coordinate of the current location of this entity (in metres).
	 */
	@Raw
	public double getXCoordinate(){
		return getPosition().getX();
	}
	
	/**
	 * Returns the y-coordinate of the current location of this entity (in metres).
	 */
	@Raw
	public double getYCoordinate(){
		return getPosition().getY();
	}
	
	/**
	 * Sets the position for this entity.
	 * 
	 * @param position	The new position to set.
	 * @post	The new position equals the given position.
	 * 			| new.getPosition().equals(position)
	 * @throws IllegalArgumentException
	 * 			Thrown when the given position isn't a valid position.
	 * 			| !Position.isValidPosition(position)
	 */
	@Raw
	protected void setPosition(Position position) throws IllegalArgumentException{
		if(!Position.isValidPosition(position))
			throw new IllegalArgumentException();
		this.position = position;
	}
	
	/**
	 * Returns the position of this entity.
	 */
	@Raw @Basic
	protected Position getPosition(){
		return position;
	}
	private Position position;
	
	/**
	 * Returns the radius of this entity (in metres).
	 */
	@Basic @Raw
	public abstract double getRadius();
	
	/**
	 * Checks whether the given radius is a valid radius for this entity.
	 * 
	 * @param radius
	 * 			The radius to check.
	 * @return	False if the radius is not a valid, finite number.
	 * 			| if(Double.isNaN(radius) || Double.isInfinite(radius))
	 * 			|		result == false
	 * @return	Whether or not the radius is bigger than the lower bound.
	 * 			| result == (radius >= getRadiusLowerBound())
	 */
	@Raw
	public boolean canHaveAsRadius(double radius){
		if(Double.isNaN(radius) || Double.isInfinite(radius))
			return false;
		return radius >= getRadiusLowerBound();
	}
	
	/**
	 * Returns a lower bound on the radius of this entity.
	 * 
	 * @return A strictly positive lower bound on the radius of this entity.
	 * 		   | result > 0
	 */
	@Raw
	public double getRadiusLowerBound(){
		return Double.MIN_VALUE;
	}
	
	/**
	 * Determines whether this entity collides with the given entity.
	 * 
	 * @param entity	The entity to check collision against.
	 * @return			Whether this entity collides with the given entity.
	 */
	public boolean collidesWith(Entity entity){
		//TODO implement
		//TODO update @return-clause with formal statement.
		return false;
	}
	
	/**
	 * Checks whether this entity is terminated.
	 */
	@Basic @Raw
	public boolean isTerminated(){
		return isTerminated;
	}
	
	/**
	 * Terminates this entity
	 *
	 * @post	This entity is terminated.
	 * 			| new.isTerminated()
	 * @post	The entity has broken its side of the association
	 *			with its world.
	 *			| !new.hasWorld()
	 * @throws	IllegalStateException
	 * 			When the world still references this Entity.
	 * 			| getWorld().hasAsEntity(this)
	 */
	@Raw
	public void terminate() throws IllegalStateException{
		if(!isTerminated()){
			if(world.hasAsEntity(this))
				throw new IllegalStateException();
			world = null;
			isTerminated = true;
		}
	}
	
	private boolean isTerminated = false;
	
	/**
	 * Checks whether this entity can be in the given world.
	 * 
	 * @param world	The world to be checked against.
	 * @return	If this entity is terminated, true if the given world is 
	 *			not effective.
	 *			If this entry is not terminated, true if
	 *			the given world is effective and not terminated.
	 *			| if(isTerminated)
	 * 			|	result == (world == null)
	 * 			| else
	 * 			|	result == (world != null && !world.isTerminated())
	 */
	public boolean canHaveAsWorld(World world){
		if(isTerminated())
			return (world == null);
		else
			return (world != null && !world.isTerminated());
	}
	
	/**
	 * Checks whether this entity is properly associated with its world.
	 * 
	 * @return	Whether this entity can have its current world as its world
	 * 			and whether its world has registered this entity.
	 * 			| result == (canHaveAsWorld(world) && world.hasAsEntity(this))
	 */
	public boolean hasProperWorld(){
		return (canHaveAsWorld(world) && world.hasAsEntity(this));
	}

	/**
	 * Checks whether this entity is in a world.
	 * 
	 * @return	True iff this entity's world is effective.
	 *			| result == (getWorld() != null)
	 */
	public boolean hasWorld(){
		return (getWorld() != null);
	}

	/**
	 * Sets this entity's world to the given world.
	 * 
	 * @param	world
	 *			The new world this entity will belong to.
	 * @post	This entity has a world.
	 *			| hasWorld()
	 * @post	This entity references the given world.
	 *			| new.getWorld() == world
	 * @throws	IllegalArgumentException
	 * 			The given world is not effective or has not (yet)
	 *			registered this entity.
	 * 			| (world == null || !world.hasAsEntity(this))
	 * @throws	IllegalArgumentException
	 * 			| If this worm can't have the given world as its world.
	 * @throws	IllegalStateException
	 * 			This entity already has a world.
	 * 			| hasWorld()
	 */
	@Raw
	public void setWorld(@Raw World world) throws IllegalArgumentException,IllegalStateException{
		if(world == null || !world.hasAsEntity(this) || !canHaveAsWorld(world))
			throw new IllegalArgumentException();
		if(hasWorld())
			throw new IllegalStateException("This entity already has a world.");
		this.world = world;
	}
	
	/**
	 * Returns the world this entity belongs to.
	 */
	public World getWorld(){
		return world;
	}
	
	private World world = null;
}
