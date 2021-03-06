package com.panda.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TraceThread {
	int threadId;
	public int getThreadId() {
		return threadId;
	}
	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}
	public List<MethodLog> getMethods() {
		return methods;
	}
	public void setMethods(List<MethodLog> methods) {
		this.methods = methods;
	}
	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	List<MethodLog> methods=new ArrayList<>();
	public final static MethodLog topMethod=new MethodLog("TopMethod");
	public final static MethodLog endMethod=new MethodLog("endMethod");
	public final static MethodLog noPart=new MethodLog("noPart");
	protected void sortMethods(){
		Stack<MethodLog>  stack=new Stack();
		int n=0;
		//stack.push(topMethod);
		for(int i=0;i<methods.size();++i){
			if(methods.get(i).action==0){
				stack.push(methods.get(i));
			}else{
				if(stack.isEmpty()){
					methods.get(i).parent=noPart;
					n++;
					continue;
				}
				stack.pop();
			}
		}
		long t0=methods.get(methods.size()-1).record.threadClockDiff;
		long t1=methods.get(methods.size()-1).record.wallClockDiff;
		for(int i=0;i<stack.size();++i){
			MethodLog np=new MethodLog("noPart",1);
			np.record.threadClockDiff=t0;
			np.record.wallClockDiff=t1;
			methods.add(np);
		}
		stack.clear();
		stack.push(topMethod);
		for(int i=0;i<n;++i){
			MethodLog np=new MethodLog("noPart",0);
			//wrong diff
			np.record.threadClockDiff=methods.get(0).record.threadClockDiff;
			np.record.wallClockDiff=methods.get(0).record.wallClockDiff;
			
			stack.push(np);
		}
		for(int i=0;i<methods.size();++i){
			if(methods.get(i).action==0){
				methods.get(i).parent=stack.get(stack.size()-1);
				stack.get(stack.size()-1).child.add(methods.get(i));
				stack.push(methods.get(i));
			}else{
				MethodLog m=stack.pop();
				m.partner=methods.get(i);
				methods.get(i).partner=m;
			}
		}
		List<MethodLog> m=new ArrayList();
		for(int i=0;i<methods.size();++i){
			if(methods.get(i).action==0){
				if(methods.get(i).FullName.equals("noPart")){
					methods.get(i).FullName=methods.get(i).partner.FullName;
					methods.get(i).record=methods.get(i).partner.record;
					methods.get(i).methodName=methods.get(i).partner.methodName;
					methods.get(i).source=methods.get(i).partner.source;
				}
				if(methods.get(i).parent==null){
					methods.get(i).parent=methods.get(i).partner.parent;
					methods.get(i).parent.child.remove(methods.get(i).partner);
					methods.get(i).parent.child.add(methods.get(i));
					methods.get(i).child=methods.get(i).partner.child;
					for(int j=0;j<methods.get(i).child.size();++j){
						methods.get(i).child.get(j).parent=methods.get(i);
					}
				}
				long beginT=methods.get(i).record.threadClockDiff;
				long endT=methods.get(i).partner.record.threadClockDiff;
				long beginW=methods.get(i).record.wallClockDiff;
				long endW=methods.get(i).partner.record.wallClockDiff;
				methods.get(i).setThreadCostTime(endT-beginT);
				methods.get(i).setWallCostTime(endW-beginW);
				m.add(methods.get(i));
			}
		}
		methods.clear();
		methods=m;
	}
}
