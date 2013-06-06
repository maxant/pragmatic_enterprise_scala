/*
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package ch.maxant.scalabook.tooling;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

public class InstanceImpl<T> implements Instance<T> {

	private List<T> values = new ArrayList<T>();
	
	public InstanceImpl(T... ts) {
		for(T t : ts){
			values.add(t);
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}

	@Override
	public T get() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAmbiguous() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUnsatisfied() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instance<T> select(Annotation... arg0) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <U extends T> Instance<U> select(Class<U> arg0,
			Annotation... arg1) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <U extends T> Instance<U> select(TypeLiteral<U> arg0,
			Annotation... arg1) {
		throw new UnsupportedOperationException();
	}

}
