package com.ck.metric;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.ck.CKMetricValues;
import com.ck.CKReport;

public class CouplingBetweenObjects extends ASTVisitor implements Metric {

	private Set<String> coupling = new HashSet<String>();

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		coupleTo(node.getType().resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		coupleTo(node.getType().resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		coupleTo(node.getType().resolveBinding());
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		coupleTo(node.getType().resolveBinding());
		return super.visit(node);
	}

	public boolean visit(ReturnStatement node) {
		if (node.getExpression() != null) {
			coupleTo(node.getExpression().resolveTypeBinding());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		coupleTo(node.resolveTypeBinding());
		coupleTo(node.getType().resolveBinding());
		return super.visit(node);
	}
	
	public boolean visit(ThrowStatement node) {
		coupleTo(node.getExpression().resolveTypeBinding());
		return super.visit(node);
	}

	public boolean visit(TypeDeclaration node) {
		ITypeBinding type = node.resolveBinding();

		ITypeBinding binding = type.getSuperclass();
		if (binding != null)
			coupleTo(binding);

		for (ITypeBinding interfaces : type.getInterfaces()) {
			coupleTo(interfaces);
		}

		return super.visit(node);
	}

	public boolean visit(MethodDeclaration node) {

		IMethodBinding method = node.resolveBinding();
		if (method == null)
			return super.visit(node);

		coupleTo(method.getReturnType());

		for (ITypeBinding param : method.getParameterTypes()) {
			coupleTo(param);
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		coupleTo(node.getType().resolveBinding());

		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {

		coupleTo(node.getRightOperand().resolveBinding());
		coupleTo(node.getLeftOperand().resolveTypeBinding());

		return super.visit(node);
	}

	public boolean visit(NormalAnnotation node) {
		coupleTo(node.resolveTypeBinding());
		return super.visit(node);
	}

	public boolean visit(MarkerAnnotation node) {
		coupleTo(node.resolveTypeBinding());
		return super.visit(node);
	}

	public boolean visit(SingleMemberAnnotation node) {
		coupleTo(node.resolveTypeBinding());
		return super.visit(node);
	}

	public boolean visit(ParameterizedType node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null)
			return super.visit(node);

		coupleTo(binding);

		for (ITypeBinding types : binding.getTypeArguments()) {
			coupleTo(types);
		}

		return super.visit(node);
	}

	private void coupleTo(ITypeBinding binding) {
		if (binding == null)
			return;
		if (binding.isWildcardType())
			return;

		String type = binding.getQualifiedName();
		if (type.equals("null"))
			return;

		if (!isFromJava(type) && !binding.isPrimitive())
			coupling.add(type.replace("[]", ""));
	}

	private boolean isFromJava(String type) {
		return type.startsWith("java.") || type.startsWith("javax.");
	}

	@Override
	public void processing(CompilationUnit cu, CKMetricValues number, CKReport report) {
		cu.accept(this);

		System.out.println(coupling);
	}

	@Override
	public void setCalculatedValues(CKMetricValues result) {
		result.setCBOValue(coupling.size());
	}
}
