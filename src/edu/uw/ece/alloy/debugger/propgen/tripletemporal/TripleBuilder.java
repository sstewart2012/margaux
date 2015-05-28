package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.CompositeOrdersIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.CompositeSizesIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.EmptinessIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.LocalityIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.OrderIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.SideIterator;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.TriplePorpertiesIterators.SizeIterator;

public class TripleBuilder {

	public final String RName;
	public final String SName, SNext, SFirst;
	public final String MiddleName,MiddleNext, MiddleFirst;
	public final String EndName, EndNext, EndFirst;

	public final String RConcreteName;
	public final String SConcreteName;
	public final String SConcreteNext;
	public final String SConcreteFirst;
	public final String MConcreteName;
	public final String EConcreteName;

	public final String EndConcreteNext, EndConcreteFirst;  
	public final String MiddleConcreteNext, MiddleConcreteFirst; 




	public TripleBuilder(String rName, String sName, String sNext,
			String sFirst, String middleName, String middleNext,
			String middleFirst, String endName, String endNext,
			String endFirst, String rConcreteName, String sConcreteName,
			String sConcreteNext, String sConcreteFirst, String mConcreteName,
			String eConcreteName, String endConcreteNext,
			String endConcreteFirst, String middleConcreteNext,
			String middleConcreteFirst) {
		super();
		RName = rName;
		SName = sName;
		SNext = sNext;
		SFirst = sFirst;
		MiddleName = middleName;
		MiddleNext = middleNext;
		MiddleFirst = middleFirst;
		EndName = endName;
		EndNext = endNext;
		EndFirst = endFirst;
		RConcreteName = rConcreteName;
		SConcreteName = sConcreteName;
		SConcreteNext = sConcreteNext;
		SConcreteFirst = sConcreteFirst;
		MConcreteName = mConcreteName;
		EConcreteName = eConcreteName;
		EndConcreteNext = endConcreteNext;
		EndConcreteFirst = endConcreteFirst;
		MiddleConcreteNext = middleConcreteNext;
		MiddleConcreteFirst = middleConcreteFirst;
	}

	public TripleBuilder(String rConcreteName, String sConcreteName,
			String sConcreteNext, String sConcreteFirst, String mConcreteName,
			String eConcreteName, String endConcreteNext,
			String endConcreteFirst, String middleConcreteNext,
			String middleConcreteFirst) {
		this(
				"r", "s", "s_next", "s_first",
				"m", "m_next", "m_first", 
				"e", "e_next", "e_first",
				rConcreteName,
				sConcreteName,
				sConcreteNext,
				sConcreteFirst,
				mConcreteName,
				eConcreteName,
				endConcreteNext,
				endConcreteFirst,
				middleConcreteNext,
				middleConcreteFirst);
	}



	public SizeProperty createSizeInstance(final Class<? extends SizeProperty> clazz, final Locality local, final Emptiness empty) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (SizeProperty) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				local, empty);
	}


	public Locality createLocalityInstance(final Class<? extends Locality> clazz, final Side side) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constructor for "+clazz);




		return (Locality) constructors[0].newInstance(RName, SName, SNext, SFirst,
				MiddleName, EndName, RConcreteName,
				SConcreteName, SConcreteNext, SConcreteFirst,
				MConcreteName, EConcreteName, side,
				"", "");
	}


	public Side createSideInstance(final Class<? extends Side> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Side) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName,
				EndNext, EndFirst, MiddleNext, MiddleFirst,
				EndConcreteNext, EndConcreteFirst,
				MiddleConcreteNext, MiddleConcreteFirst);

	}

	public Emptiness createEmptinessInstance(final Class<? extends Emptiness> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);


		return (Emptiness) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName);
	}

	public Order createOrderInstance(final Class<? extends Order> clazz, final SizeProperty size) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (Order) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, size);
	}

	public CompositeOrders createCompositeOrdersInstance(final Class<? extends CompositeOrders> clazz, final Order order1, final Order order2) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (CompositeOrders) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, order1, order2);
	}
	
	public CompositeSizes createCompositeSizesInstance(final Class<? extends CompositeSizes> clazz, final SizeProperty size1, final SizeProperty size2) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		Constructor<?>[] constructors = clazz.getConstructors();

		if(constructors.length != 1)
			throw new RuntimeException("There has to be only one constrcutor for "+clazz);

		return (CompositeSizes) constructors[0].newInstance(RName, SName, SNext, 
				SFirst, MiddleName, EndName, 
				RConcreteName, SConcreteName, SConcreteNext, 
				SConcreteFirst, MConcreteName,  EConcreteName, size1, size2);
	}

	public Map<String, Pair<String,String>> getAllProperties(){

		final TriplePorpertiesIterators iterators = new TriplePorpertiesIterators(this);


		//A map from each call to the actual pred
		Map<String, Pair<String, String>> preds = new TreeMap<>();
		Set<String> revComposite = new HashSet<String>();

		for(Side side: iterators. new SideIterator(this)){
			for(Locality local: iterators. new LocalityIterator(this, side)){
				for(Emptiness empty: iterators. new EmptinessIterator(this)){
					for(SizeProperty size: iterators. new SizeIterator(this, local, empty)){
						if(!size.isConsistent()) continue;
						preds.put(size.genPredName(), new Pair(size.genPredCall(),  size.generateProp()));
						for(Order order: iterators. new OrderIterator(this, size)){
							if(!order.isConsistent()) continue;
							preds.put(order.genPredName(), new Pair(order.genPredCall(),  order.generateProp()));
							
							//Composite structures for two size and orders
							for(SizeProperty size2: iterators. new SizeIterator(this, local, empty)){
								if(!size2.isConsistent()) continue;
								
								//record the reverse in advance
								for(CompositeSizes compositeSizes: iterators. new CompositeSizesIterator(this, size2, size)){
									if(!compositeSizes.isConsistent()) continue;
									//Add to the list here
									revComposite.add(compositeSizes.genPredName());
								}
								for(CompositeSizes compositeSizes: iterators. new CompositeSizesIterator(this, size, size2)){
									if(!compositeSizes.isConsistent()) continue;
									if(revComposite.contains(compositeSizes.genPredName())) break;
									//Add to the list here
									preds.put(compositeSizes.genPredName(), new Pair(compositeSizes.genPredCall(),  compositeSizes.generateProp()));
								}

								for(Order order2: iterators. new OrderIterator(this, size2)){
									if(!order2.isConsistent()) continue;
									
									//record the reverse in advance
									for(CompositeOrders compositeOrders: iterators. new CompositeOrdersIterator(this, order2, order)){
										if(!compositeOrders.isConsistent()) continue;
										//Add to the list here
										revComposite.add(compositeOrders.genPredName());
									}
									
									for(CompositeOrders compositeOrders: iterators. new CompositeOrdersIterator(this, order, order2)){
										if(!compositeOrders.isConsistent()) continue;
										if(revComposite.contains(compositeOrders.genPredName())) break;
										//Add to the list here
										preds.put(compositeOrders.genPredName(), new Pair(compositeOrders.genPredCall(),  compositeOrders.generateProp()));
									}
									
								}
								
							}
							
							
							
						}
						
					}
				}
			}
		}

		return Collections.unmodifiableMap(preds);
	}

}
